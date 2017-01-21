/**
 *
 */
package logbook.internal;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import logbook.builtinscript.BuiltinScriptKt;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Display;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.dto.BattleExDto;
import logbook.dto.BattleResultDto;
import logbook.gui.ApplicationMain;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.DateTimeString;
import logbook.gui.logic.IntegerPair;
import logbook.gui.logic.TableRowHeader;
import logbook.scripting.BattleLogListener;
import logbook.scripting.BattleLogProxy;
import logbook.scripting.BuiltinScriptFilter;
import logbook.scripting.CombatLogProxy;
import logbook.util.ReportUtils;

/**
 * @author Nekopanda
 * 出撃ログの保存・読み込み
 */
public class BattleResultServer {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(BattleResultServer.class);

    private static DateFormat format = new SimpleDateFormat(AppConstants.BATTLE_LOGFILE_DATE_FORMAT);

    private static Schema<BattleExDto> schema = RuntimeSchema.getSchema(BattleExDto.class);

    private static class BattleResult extends BattleResultDto {
        public DataFile file;
        public int index;

        BattleResult(BattleExDto dto, DataFile file, int index, Comparable[] extData,Map<String,String[][]> builtinCombatExtData,
                Map<String, Comparable[][]> combatExtData) {
            super(dto, extData,builtinCombatExtData, combatExtData);
            this.file = file;
            this.index = index;
        }
    }

    private static String logPath = null;
    private static volatile BattleResultServer instance = new BattleResultServer();

    private static List<Runnable> eventListeners = new ArrayList<>();

    public static void setLogPath(String path) {
        logPath = path;
    }

    public static void addListener(Runnable listener) {
        eventListeners.add(listener);
    }

    public static void removeListener(Runnable listener) {
        eventListeners.remove(listener);
    }

    private static void fireEvent() {
        for (Runnable listener : eventListeners) {
            listener.run();
        }
    }

    public static void load() {
        final BattleResultServer data = new BattleResultServer(logPath);
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                // 一時的にストアしてたのを処理する
                for (BattleExDto dto : instance.tmpDat) {
                    data.addNewResult(dto);
                }
                instance = data;
                fireEvent();

            }
        });
        data.isLoaded_ = true;
    }

    public static void dispose() {
        instance = null;
    }

    public static BattleResultServer get() {
        return instance;
    }

    // member
    private final String path;
    private final LinkedBuffer buffer = LinkedBuffer.allocate(128 * 1024);

    // フィルタ用
    private Date firstBattleTime;
    private Date lastBattleTime;
    private final Set<String> dropShipList = new TreeSet<String>();
    private final Set<IntegerPair> mapList = new TreeSet<IntegerPair>();
    private final Set<Integer> cellList = new TreeSet<Integer>();

    private final List<BattleResult> resultList = new ArrayList<BattleResult>();
    private final Map<String, DataFile> fileMap = new HashMap<>();

    // 重複検出用
    private final Set<Date> resultDateSet = new HashSet<Date>();

    // キャッシュ
    private DataFile cachedFile;
    private List<BattleExDto> cachedResult;

    // 一時ストア
    private List<BattleExDto> tmpDat = null;

    private int failCount = 0;

    private boolean isLoaded_ = false;
    private boolean isLoadCombatLog = AppConfig.get().isLoadCombatLog();

    private abstract class DataFile {
        final File file;
        int numRecords = 0;

        public DataFile(File file) {
            this.file = file;
        }

        public List<BattleExDto> readAll() throws IOException {
            throw new UnsupportedOperationException();
        }

        public List<BattleExDto> readAllWithoutReadFromJson(LinkedBuffer buffer) throws IOException {
            throw new UnsupportedOperationException();
        }
        public List<BattleExDto> readAllWithoutReadFromJson() throws IOException {
            return readAllWithoutReadFromJson(BattleResultServer.this.buffer);
        }

        public String getPath() {
            throw new UnsupportedOperationException();
        }

        public void addToFile(BattleExDto dto) {
            throw new UnsupportedOperationException();
        }

        public int getNumRecords() {
            return this.numRecords;
        }

        List<BattleExDto> load(InputStream input) throws IOException {
            List<BattleExDto> result = BattleResultServer.this.loadFromInputStream(input,
                    BattleResultServer.this.buffer);
            this.numRecords = result.size();
            return result;
        }

        List<BattleExDto> loadWithoutReadFromJson(InputStream input) throws IOException {
            return loadWithoutReadFromJson(input, BattleResultServer.this.buffer);
        }

        List<BattleExDto> loadWithoutReadFromJson(InputStream input, LinkedBuffer buffer) throws IOException {
            List<BattleExDto> result = BattleResultServer.this.loadFromInputStreamWithoutReadFromJson(input,buffer);
            this.numRecords = result.size();
            return result;
        }
    }

    private class NormalDataFile extends DataFile {

        public NormalDataFile(File file) {
            super(file);
        }

        @Override
        public List<BattleExDto> readAll() throws IOException {
            try (InputStream input = new FileInputStream(this.file)) {
                return this.load(input);
            }
        }

        @Override
        public List<BattleExDto> readAllWithoutReadFromJson(LinkedBuffer buffer) throws IOException {
            try (InputStream input = new FileInputStream(this.file)) {
                return this.loadWithoutReadFromJson(input,buffer);
            }
        }

        @Override
        public String getPath() {
            return this.file.getAbsolutePath();
        }

        @Override
        public void addToFile(BattleExDto dto) {
            // ファイルとリストに追加
            try (FileOutputStream output = new FileOutputStream(getStoreFile(this.file), true)) {
                ProtostuffIOUtil.writeDelimitedTo(output, dto, schema, BattleResultServer.this.buffer);
                BattleResultServer.this.buffer.clear();
            } catch (IOException e) {
                LOG.get().warn("出撃ログの書き込みに失敗しました", e);
            }
            ++this.numRecords;
        }
    }

    private class ZipDataFile extends DataFile {

        private final String zipName;

        public ZipDataFile(File file, String zipName) {
            super(file);
            this.zipName = zipName;
        }

        @Override
        public List<BattleExDto> readAll() throws IOException {
            try (ZipFile zipFile = new ZipFile(this.file)) {
                try (InputStream input = zipFile.getInputStream(zipFile.getEntry(this.zipName))) {
                    return this.load(input);
                }
            }
        }

        @Override
        public List<BattleExDto> readAllWithoutReadFromJson(LinkedBuffer buffer) throws IOException {
            try (ZipFile zipFile = new ZipFile(this.file)) {
                try (InputStream input = zipFile.getInputStream(zipFile.getEntry(this.zipName))) {
                    return this.loadWithoutReadFromJson(input,buffer);
                }
            }
        }

        @Override
        public String getPath() {
            return this.file.getAbsolutePath() + ":" + this.zipName;
        }
    }

    private List<BattleExDto> loadFromInputStream(InputStream input, LinkedBuffer buffer) throws IOException {
        List<BattleExDto> result = new ArrayList<BattleExDto>();
        try {
            while (input.available() > 0) {
                BattleExDto battle = schema.newMessage();
                ProtostuffIOUtil.mergeDelimitedFrom(input, battle, schema, buffer);
                try {
                    battle.readFromJson();
                    result.add(battle);
                } catch (Exception e) {
                    this.failCount++;
                    LOG.get().warn("戦闘ログの読み込みに失敗しました(" + new DateTimeString(battle.getBattleDate()) + ")", e);
                }
            }
        } catch (EOFException e) {
        }
        return result;
    }
    ///json読みをこの段階でやらない
    private List<BattleExDto> loadFromInputStreamWithoutReadFromJson(InputStream input, LinkedBuffer buffer) throws IOException {
        List<BattleExDto> result = new ArrayList<BattleExDto>();
        try {
            while (input.available() > 0) {
                BattleExDto battle = schema.newMessage();
                ProtostuffIOUtil.mergeDelimitedFrom(input, battle, schema, buffer);
                try {
                    result.add(battle);
                } catch (Exception e) {
                    this.failCount++;
                    LOG.get().warn("戦闘ログの読み込みに失敗しました(" + new DateTimeString(battle.getBattleDate()) + ")", e);
                }
            }
        } catch (EOFException e) {
        }
        return result;
    }
    private BattleResultServer() {
        this.path = null;
        this.firstBattleTime = new Date();
        this.lastBattleTime = new Date();
        // とりあえず貯める
        this.tmpDat = new ArrayList<>();
    }

    private BattleResultServer(String path) {
        this.path = path;
        // ファイルを読み込んで resultList を作成
        File dir = new File(path);
        if (dir.exists()) {
            // ファイルリストを作成
            for (File file : FileUtils.listFiles(dir, new String[] { "dat", "zip" }, true)) {
                try {
                    if (file.getName().endsWith("dat")) {
                        DataFile dataFile = new NormalDataFile(file);
                        this.fileMap.put(dataFile.getPath(), dataFile);
                    }
                    else {
                        try (ZipFile zipFile = new ZipFile(file)) {
                            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                            while (enumeration.hasMoreElements()) {
                                ZipEntry entry = enumeration.nextElement();
                                DataFile dataFile = new ZipDataFile(file, entry.getName());
                                this.fileMap.put(dataFile.getPath(), dataFile);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOG.get().warn("出撃ログの読み込みに失敗しました (" + file.getPath() + ")", e);
                }
            }
            this.reloadFiles();
        }

        // フィルタ用パラメータを計算
        this.firstBattleTime = new Date();
        this.lastBattleTime = new Date(0);
        for (BattleResult battle : this.resultList) {
            this.update(battle);
        }
    }

    public void reloadFiles() {
        this.resultDateSet.clear();
        this.resultList.clear();
        this.failCount = 0;

        BattleLogListener battleLogScript = BattleLogProxy.get();

        battleLogScript.begin();
        CombatLogProxy.beginAll();
        //新しい順
        List<DataFile> sortedFileList =
            this.fileMap.values()
            .stream()
            .sorted((f1,f2)->f2.file.getName().compareTo(f1.file.getName()))
            .collect(Collectors.toList());

        List<CompletableFuture<List<BattleResult>>> futures = new ArrayList<>();
        ConcurrentLinkedQueue<LinkedBuffer> bufferQueue = new ConcurrentLinkedQueue<>();
        final int parallel = 2;
        ExecutorService executer = Executors.newFixedThreadPool(parallel);
        for(int i=0;i<parallel;i++){
            bufferQueue.add(LinkedBuffer.allocate(128*1024));
        }
        for (DataFile file:sortedFileList) {
            CompletableFuture<List<BattleResult>> future = CompletableFuture.supplyAsync(
                () -> {
                    LinkedBuffer buffer = bufferQueue.poll();
                    if(buffer == null){
                        buffer = LinkedBuffer.allocate(128*1024);
                    }
                    List<BattleResult> list = loadBattleResults(file, this.isLoadCombatLog, this.resultDateSet, buffer);
                    bufferQueue.add(buffer);
                    return list;
                },
                executer
            );
            futures.add(future);
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        executer.shutdown();
        for(CompletableFuture<List<BattleResult>> result: futures){
            this.resultList.addAll(result.join());
        }

        CombatLogProxy.endAll();
        battleLogScript.end();

        // 時刻でソート
        Collections.sort(this.resultList, new Comparator<BattleResult>() {
            @Override
            public int compare(BattleResult arg0, BattleResult arg1) {
                return Long.compare(
                        arg0.getBattleDate().getTime(), arg1.getBattleDate().getTime());
            }
        });

        fireEvent();
    }

    private void update(BattleResultDto battle) {
        Date battleDate = battle.getBattleDate();
        if (battleDate.before(this.firstBattleTime)) {
            this.firstBattleTime = battleDate;
        }
        if (battleDate.after(this.lastBattleTime)) {
            this.lastBattleTime = battleDate;
        }
        if (battle.isPractice() == false) {
            if (!StringUtils.isEmpty(battle.getDropName())) {
                this.dropShipList.add(battle.getDropName());
            }
            if (!StringUtils.isEmpty(battle.getDropItemName())) {
                this.dropShipList.add(battle.getDropItemName());
            }

            int[] map = battle.getMapCell().getMap();
            this.mapList.add(new IntegerPair(map[0], map[1], "%d-%d"));
            this.cellList.add(map[2]);
        }
    }

    public void addNewResult(BattleExDto dto) {
        // ファイルとリストに追加
        if (dto.isCompleteResult()) {
            if (this.tmpDat != null) {
                this.tmpDat.add(dto);
            }
            else {
                File file = new File(FilenameUtils.concat(this.path, format.format(dto.getBattleDate()) + ".dat"));
                DataFile dataFile = this.fileMap.get(file.getAbsolutePath());
                if (dataFile == null) {
                    dataFile = new NormalDataFile(file);
                    this.fileMap.put(dataFile.getPath(), dataFile);
                }

                BattleLogListener battleLogScript = BattleLogProxy.get();
                BattleResult resultEntry = createBattleResult(dto, dataFile, dataFile.getNumRecords(), this.isLoadCombatLog);
                this.update(resultEntry);
                this.resultList.add(resultEntry);

                dataFile.addToFile(dto);

                // キャッシュされているときはキャッシュにも追加
                if ((this.cachedFile != null) && (dataFile == this.cachedFile)) {
                    this.cachedResult.add(dto);
                }
            }

            fireEvent();
        }
    }

    public int size() {
        return this.resultList.size();
    }

    public BattleResultDto[] getList() {
        return this.resultList.toArray(new BattleResultDto[this.resultList.size()]);
    }

    public List<BattleResultDto> getFilteredList(BattleResultFilter filter) {
        List<BattleResultDto> list = new ArrayList<BattleResultDto>();
        for (BattleResult result : this.resultList) {
            BattleResultDto dto = result;
            if (this.matchFilter(filter, dto)) {
                list.add(dto);
            }
        }
        return list;
    }

    /** 出撃ログがフィルタにマッチしているかどうか
     * @param filter
     * @param dto
     * @return
     */
    private boolean matchFilter(BattleResultFilter filter, BattleResultDto dto) {
        if ((filter.fromTime != null) && filter.fromTime.after(dto.getBattleDate())) {
            return false;
        }
        if ((filter.toTime != null) && filter.toTime.before(dto.getBattleDate())) {
            return false;
        }
        if ((filter.dropShip != null) &&
                (filter.dropShip.equals(dto.getDropName()) == false) &&
                (filter.dropShip.equals(dto.getDropItemName()) == false)) {
            return false;
        }
        if (filter.timeSpan != null) {
            Date from = filter.timeSpan.getFrom();
            Date to = filter.timeSpan.getTo();
            if (from.after(dto.getBattleDate())) {
                return false;
            }
            if (to.before(dto.getBattleDate())) {
                return false;
            }
        }
        if ((filter.map != null)) {
            if (dto.isPractice()) {
                return false;
            }
            int[] battleMap = dto.getMapCell().getMap();
            if (filter.map.compareTo(new IntegerPair(battleMap[0], battleMap[1], "%d-%d")) != 0) {
                return false;
            }
        }
        if ((filter.cell != null)) {
            if (dto.isPractice()) {
                return false;
            }
            int[] battleMap = dto.getMapCell().getMap();
            if (filter.cell != battleMap[2]) {
                return false;
            }
        }
        if ((filter.rankCombo != null) &&
                (filter.rankCombo.indexOf(dto.getRank().rank().charAt(0)) == -1)) {
            return false;
        }
        if (filter.printPractice != null) {
            // 排他的論理和です
            if (dto.isPractice() ^ filter.printPractice) {
                return false;
            }
        }
        return true;
    }

    /** 詳細を読み込む（失敗したら null ） */
    public BattleExDto getBattleDetail(BattleResultDto summary) {
        BattleResult result = (BattleResult) summary;
        if ((this.cachedFile == null) || (result.file != this.cachedFile)) {
            try {
                this.cachedResult = result.file.readAll();
                this.cachedFile = result.file;
            } catch (IOException e) {
                return null;
            }
        }
        if (this.cachedResult.size() <= result.index) {
            return null;
        }
        return this.cachedResult.get(result.index);
    }

    public Date getFirstBattleTime() {
        return this.firstBattleTime;
    }

    public Date getLastBattleTime() {
        return this.lastBattleTime;
    }

    public List<String> getDropShipList() {
        return new ArrayList<String>(this.dropShipList);
    }

    public List<IntegerPair> getMapList() {
        return new ArrayList<IntegerPair>(this.mapList);
    }

    public List<Integer> getCellList() {
        return new ArrayList<Integer>(this.cellList);
    }

    private static File getStoreFile(File file) throws IOException {
        // 報告書の保存先にファイルを保存します
        File dir = file.getParentFile();
        if ((dir == null) || !(dir.exists() || dir.mkdirs())) {
            // 報告書の保存先ディレクトリが無く、ディレクトリの作成に失敗した場合はカレントフォルダにファイルを保存
            file = new File(file.getName());
        }
        File altFile = new File(FilenameUtils.removeExtension(file.getPath()) + "_alternativefile.dat");
        if (ReportUtils.isLocked(file)) {
            // ロックされている場合は代替ファイルに書き込みます
            file = altFile;
        }
        else {
            if (altFile.exists() && !ReportUtils.isLocked(altFile) && (FileUtils.sizeOf(altFile) > 0)) {
                mergeAltFile(file, altFile);
            }
        }
        return file;
    }

    /**
     * alternativeファイルを本体にマージして削除します
     *
     * @param report ファイル本体
     * @param alt_report alternativeファイル
     * @return
     * @throws IOException
     */
    private static void mergeAltFile(File report, File alt_report) throws IOException {
        // report が空ファイルの場合は、alt ファイルをリネームして終了
        if (!report.exists() || (FileUtils.sizeOf(report) <= 0)) {
            report.delete();
            alt_report.renameTo(report);
            return;
        }
        try (OutputStream report_stream = new FileOutputStream(report, true)) {
            try (InputStream alt_stream = new FileInputStream(alt_report)) {
                IOUtils.copy(alt_stream, report_stream);
            }
        }
        alt_report.delete();
    }

    /**
     * 読み取りに失敗した戦闘数
     * @return failCount
     */
    public int getFailCount() {
        return this.failCount;
    }
    public static void writeBuiltinCsvWithFilter(String[] sourcePaths, String targetPath, BuiltinScriptFilter filter) {
        Set<Date> resultDateSet = Collections.synchronizedSet(new HashSet<Date>());
        int n = 0;
        BattleResultServer self = new BattleResultServer();
        BattleLogProxy.get().begin();
        for (String sourcePath : sourcePaths) {
            File sourceFile = new File(sourcePath);
            if (sourceFile.isDirectory()) {
                Map<String, File> files = new TreeMap<String, File>();
                for (File file : FileUtils.listFiles(sourceFile, new String[] { "dat", "zip" }, true)) {
                    files.put(file.getPath(), file);
                }
                for (File file : files.values()) {
                    n = writeBuiltinCsvWithFilter(self, file, targetPath, resultDateSet, n,filter);
                }
            }
            else if (sourceFile.isFile()) {
                n = writeBuiltinCsv(self, sourceFile, targetPath, resultDateSet, n);
            }
        }
        BattleLogProxy.get().end();
    }
    private static int writeBuiltinCsvWithFilter(DataFile file, String targetPath, Set<Date> resultDateSet, int n, BuiltinScriptFilter filter) {
        return writeBuiltinCsvWithFilter(loadBuiltinBattleResultsWithFilter(file, resultDateSet,filter), targetPath, n,filter);
    }
    private static int writeBuiltinCsvWithFilter(BattleResultServer self, File file, String targetPath, Set<Date> resultDateSet, int n, BuiltinScriptFilter filter) {
        try {
            if (file.getName().endsWith(".dat")) {
                DataFile dataFile = self.new NormalDataFile(file);
                n = writeBuiltinCsvWithFilter(dataFile,targetPath ,resultDateSet,n ,filter);
            }
            else if (file.getName().endsWith(".zip")) {
                try (ZipFile zipFile = new ZipFile(file)) {
                    Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                    while (enumeration.hasMoreElements()) {
                        ZipEntry entry = enumeration.nextElement();
                        DataFile dataFile = self.new ZipDataFile(file, entry.getName());
                        n = writeBuiltinCsvWithFilter(dataFile, targetPath, resultDateSet,n ,filter);
                    }
                }
            }
            return n;
        } catch (IOException e) {
            LOG.get().warn("出撃ログの読み込みに失敗しました (" + file.getPath() + ")", e);
            return n;
        }
    }
    private static List<String[][]> loadBuiltinBattleResultsWithFilter(DataFile file,Set<Date> resultDateSet,BuiltinScriptFilter filter) {
        try {
            List<BattleExDto> battleAll = file.readAllWithoutReadFromJson();
            battleAll
                .parallelStream()
                .forEach(b->b.readFromJson());
            ArrayList<BattleExDto> battle = new ArrayList<BattleExDto>();
            for(BattleExDto b : battleAll){
                if (b.isCompleteResult() && !resultDateSet.contains(b.getBattleDate())) {
                    resultDateSet.add(b.getBattleDate());
                    battle.add(b);
                }
            }
            List<String[][]> result=
                battle
                .parallelStream()
                .map(b->BuiltinScriptKt.BodyWithFilter(b,filter))
                .collect(Collectors.toList());
            ApplicationMain.logPrint("読み込み完了(" + new File(file.getPath()).getName() + ")");
            return result;
        } catch (Exception e) {
            LOG.get().warn("出撃ログの読み込みに失敗しました (" + file.getPath() + ")", e);
            return new ArrayList<String[][]>();
        }
    }
    private static int writeBuiltinCsvWithFilter(List<String[][]> resultList, String targetPath, int n,BuiltinScriptFilter filter) {
        try {
            boolean append = n > 0;
            int start = n;
            String[] header = BuiltinScriptKt.HeaderWithKey(filter.key);
            List<String[]> allBodies = new ArrayList<String[]>();
            for (String[][] item : resultList) {
                for (String[] body : item) {
                    allBodies.add(ArrayUtils.addAll(new String[] { (new Integer(++n)).toString() }, body));
                }
            }
            if(n == start){return n;}
            String[] headerArray = header;
            CreateReportLogic.writeCsvRed(new File(targetPath), ArrayUtils.addAll(new String[] { "No." }, headerArray), allBodies, append);

            return n;
        } catch (IOException e) {
            LOG.get().warn("出撃ログの書き込みに失敗しました", e);
            return n;
        }
    }

    public static void writeBuiltinCsv(String[] sourcePaths, String targetPath) {
        Set<Date> resultDateSet = Collections.synchronizedSet(new HashSet<Date>());
        int n = 0;
        BattleResultServer self = new BattleResultServer();
        BattleLogProxy.get().begin();
        for (String sourcePath : sourcePaths) {
            File sourceFile = new File(sourcePath);
            if (sourceFile.isDirectory()) {
                Map<String, File> files = new TreeMap<String, File>();
                for (File file : FileUtils.listFiles(sourceFile, new String[] { "dat", "zip" }, true)) {
                    files.put(file.getPath(), file);
                }
                for (File file : files.values()) {
                    n = writeBuiltinCsv(self, file, targetPath, resultDateSet, n);
                }
            }
            else if (sourceFile.isFile()) {
                n = writeBuiltinCsv(self, sourceFile, targetPath, resultDateSet, n);
            }
        }
        BattleLogProxy.get().end();
    }
    private static int writeBuiltinCsv(DataFile file, String targetPath, Set<Date> resultDateSet, int n) {
        return writeBuiltinCsv(loadBuiltinBattleResults(file, resultDateSet), targetPath, n);
    }
    private static int writeBuiltinCsv(BattleResultServer self, File file, String targetPath, Set<Date> resultDateSet, int n) {
        try {
            if (file.getName().endsWith(".dat")) {
                DataFile dataFile = self.new NormalDataFile(file);
                n = writeBuiltinCsv(dataFile, targetPath, resultDateSet, n);
            }
            else if (file.getName().endsWith(".zip")) {
                try (ZipFile zipFile = new ZipFile(file)) {
                    Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                    while (enumeration.hasMoreElements()) {
                        ZipEntry entry = enumeration.nextElement();
                        DataFile dataFile = self.new ZipDataFile(file, entry.getName());
                        n = writeBuiltinCsv(dataFile, targetPath, resultDateSet, n);
                    }
                }
            }
            return n;
        } catch (IOException e) {
            LOG.get().warn("出撃ログの読み込みに失敗しました (" + file.getPath() + ")", e);
            return n;
        }
    }
    private static int writeBuiltinCsv(List<Map<String,String[][]>> resultList, String targetPath, int n) {
        try {
            boolean append = n > 0;
            Map<String,String[]> header = BuiltinScriptKt.AllHeader();
            Set<String>keySet = header.keySet();
            for(String key:keySet){
                int start = n;
                List<String[]> allBodies = new ArrayList<String[]>();
                for (Map<String,String[][]> item : resultList) {
                    for (String[] body : item.get(key)) {
                        allBodies.add(ArrayUtils.addAll(new String[] { (new Integer(++n)).toString() }, body));
                    }
                }
                if(append && n == start){ continue; }
                String[] headerArray = header.get(key);
                CreateReportLogic.writeCsvRed(new File(targetPath).toPath().resolve(key + ".csv").toFile(), ArrayUtils.addAll(new String[] { "No." }, headerArray), allBodies, append);
            }
            return n;
        } catch (IOException e) {
            LOG.get().warn("出撃ログの書き込みに失敗しました", e);
            return n;
        }
    }
    private static List<Map<String,String[][]>> loadBuiltinBattleResults(DataFile file,Set<Date> resultDateSet) {
        try {
            List<BattleExDto> battleAll = file.readAllWithoutReadFromJson();
            battleAll
                .parallelStream()
                .forEach(b->b.readFromJson());
            ArrayList<BattleExDto> battle = new ArrayList<BattleExDto>();
            for(BattleExDto b : battleAll){
                if (b.isCompleteResult() && !resultDateSet.contains(b.getBattleDate())) {
                    resultDateSet.add(b.getBattleDate());
                    battle.add(b);
                }
            }
            List<Map<String,String[][]>> result=
                battle
                .parallelStream()
                //.map(b->b.BuiltinScriptBody())
                .map(b-> BuiltinScriptKt.AllBody(b))
                .collect(Collectors.toList());
            ApplicationMain.logPrint("読み込み完了(" + new File(file.getPath()).getName() + ")");
            return result;
        } catch (Exception e) {
            LOG.get().warn("出撃ログの読み込みに失敗しました (" + file.getPath() + ")", e);
            return new ArrayList<Map<String,String[][]>>();
        }
    }

    public static List<Comparable[]> loadBuiltinBattleResultsBody(String key,List<BattleResultDto> targets){
        DataFile nowFile = null;
        List<BattleResult> resultList = new ArrayList<>();
        List<Comparable[][]>bodyList = new ArrayList<>();
        for(BattleResultDto item:targets){
            if(item instanceof BattleResult){
                BattleResult result = (BattleResult)item;
                if(nowFile == null || !nowFile.file.equals(result.file.file)){
                    if(resultList.size() > 0){
                        bodyList.addAll(loadBuiltinBattleResults(key, nowFile, resultList));
                    }
                    resultList.clear();
                    nowFile = result.file;
                }
                resultList.add(result);
            }
        }
        if(resultList.size() > 0){
            bodyList.addAll(loadBuiltinBattleResults(key, nowFile, resultList));
        }
        List<Comparable[]>body = new ArrayList<>();
        for(Comparable[][] item:bodyList){
            for(Comparable[] line:item){
                body.add(line);
            }
        }
        for(int i=0;i<body.size();i++){
            TableRowHeader header = (TableRowHeader)body.get(i)[0];
            header.setNumber(i+1);
        }
        return body;
    }
    private static List<Comparable[][]> loadBuiltinBattleResults(String key,DataFile file,List<BattleResult> resultList) {
        try {
            List<BattleExDto> battleAll = file.readAllWithoutReadFromJson();
            ArrayList<BattleExDto> battle = new ArrayList<BattleExDto>();
            Map<Date,BattleResult> map = new HashMap<>();
            for(BattleResult item:resultList){
                map.put(item.getBattleDate(), item);
            }
            for(BattleExDto b : battleAll){
                if (b.isCompleteResult() && map.containsKey(b.getBattleDate())) {
                    battle.add(b);
                }
            }
            List<Comparable[][]> result=
                battle
                .parallelStream()
                .map(b->{
                    b.readFromJson();
                    BattleResult item = map.get(b.getBattleDate());
                    String[][] body = BuiltinScriptKt.BodyWithKey(key,b,null);
                    List<Comparable[]> list = new ArrayList<>();
                    for(String[] line:body){
                        list.add(ArrayUtils.addAll(new Comparable[] { new TableRowHeader(0, item) },line));
                    }
                    return list.toArray(new Comparable[0][]);
                })
                .collect(Collectors.toList());
            return result;
        } catch (Exception e) {
            return new ArrayList<Comparable[][]>();
        }
    }



    public static void writeCsv(String[] sourcePaths, String targetPath) {
        Set<Date> resultDateSet = new HashSet<Date>();
        int n = 0;
        BattleResultServer self = new BattleResultServer();
        BattleLogProxy.get().begin();
        CombatLogProxy.beginAll();
        for (String sourcePath : sourcePaths) {
            File sourceFile = new File(sourcePath);
            if (sourceFile.isDirectory()) {
                Map<String, File> files = new TreeMap<String, File>();
                for (File file : FileUtils.listFiles(sourceFile, new String[] { "dat", "zip" }, true)) {
                    files.put(file.getPath(), file);
                }
                for (File file : files.values()) {
                    n = writeCsv(self, file, targetPath, resultDateSet, n);
                }
            }
            else if (sourceFile.isFile()) {
                n = writeCsv(self, sourceFile, targetPath, resultDateSet, n);
            }
        }
        CombatLogProxy.endAll();
        BattleLogProxy.get().end();
    }

    private static int writeCsv(BattleResultServer self, File file, String targetPath, Set<Date> resultDateSet, int n) {
        try {
            if (file.getName().endsWith(".dat")) {
                DataFile dataFile = self.new NormalDataFile(file);
                n = writeCsv(dataFile, targetPath, resultDateSet, n);
            }
            else if (file.getName().endsWith(".zip")) {
                try (ZipFile zipFile = new ZipFile(file)) {
                    Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                    while (enumeration.hasMoreElements()) {
                        ZipEntry entry = enumeration.nextElement();
                        DataFile dataFile = self.new ZipDataFile(file, entry.getName());
                        n = writeCsv(dataFile, targetPath, resultDateSet, n);
                    }
                }
            }
            return n;
        } catch (IOException e) {
            LOG.get().warn("出撃ログの読み込みに失敗しました (" + file.getPath() + ")", e);
            return n;
        }
    }

    private static int writeCsv(DataFile file, String targetPath, Set<Date> resultDateSet, int n) {
        return writeCsv(loadBattleResultsWriteCsv(file,resultDateSet), targetPath, n);
    }

    private static List<BattleResult> loadBattleResults(DataFile file, boolean isLoadCombatLog, Set<Date> resultDateSet,LinkedBuffer buffer) {
        try {
            List<BattleExDto> battleAll = file.readAllWithoutReadFromJson(buffer);
            battleAll
                .parallelStream()
                .forEach(b->b.readFromJson());
            ArrayList<BattleExDto> battle = new ArrayList<BattleExDto>();
            battleAll
                .stream()
                .forEach(b->{
                        if (b.isCompleteResult() && !resultDateSet.contains(b.getBattleDate())) {
                            resultDateSet.add(b.getBattleDate());
                            battle.add(b);
                        }
                    });

            List<Comparable[]> battleLog =
                battle
                    .parallelStream()
                    .map(b->BattleLogProxy.get().bodyMT(b))
                    .collect(Collectors.toList());
            List<Map<String,String[][]>> builtinCombatLog =
                battle
                    .parallelStream()
                    .map(b->BuiltinScriptKt.AllBody(b))
                    .collect(Collectors.toList());

            List<Map<String,Comparable[][]>> combatLog = null;
            if(isLoadCombatLog){
                combatLog =
                    battle
                    .parallelStream()
                    .map(b->CombatLogProxy.bodyAllMT(b))
                    .collect(Collectors.toList());
            }

            List<BattleResult> result = new ArrayList<BattleResult>();
            for(int i=0;i<battle.size();i++){
                result.add(createBattleResultWithCombatLog(battle.get(i),file, i,battleLog.get(i),builtinCombatLog.get(i),(isLoadCombatLog)?combatLog.get(i) :null));
            }
            ApplicationMain.logPrint("読み込み完了(" + new File(file.getPath()).getName() + ")");
            return result;
        } catch (Exception e) {
            LOG.get().warn("出撃ログの読み込みに失敗しました (" + file.getPath() + ")", e);
            return new ArrayList<BattleResult>();
        }
    }
    private static List<Map<String,Comparable[][]>> loadBattleResultsWriteCsv(DataFile file,Set<Date> resultDateSet) {
        try {
            List<BattleExDto> battleAll = file.readAllWithoutReadFromJson();
            battleAll
                .parallelStream()
                .forEach(b->b.readFromJson());
            ArrayList<BattleExDto> battle = new ArrayList<BattleExDto>();
            battleAll
                .stream()
                .forEach(b->{
                        if (b.isCompleteResult() && !resultDateSet.contains(b.getBattleDate())) {
                            resultDateSet.add(b.getBattleDate());
                            battle.add(b);
                        }
                    });
            List<Map<String,Comparable[][]>> combatLog =
                battle
                    .parallelStream()
                    .map(b->CombatLogProxy.bodyAllMT(b))
                    .collect(Collectors.toList());
            ApplicationMain.logPrint("読み込み完了(" + new File(file.getPath()).getName() + ")");
            return combatLog;
        } catch (Exception e) {
            LOG.get().warn("出撃ログの読み込みに失敗しました (" + file.getPath() + ")", e);
            return new ArrayList<Map<String,Comparable[][]>>();
        }
    }

    private static int writeCsv(List<Map<String,Comparable[][]>> resultList, String targetPath, int n) {
        try {
            boolean append = n > 0;
            for (CombatLogProxy proxy : CombatLogProxy.getAll()) {
                int start = n;
                List<Comparable[]> allBodies = new ArrayList<Comparable[]>();
                for (Map<String,Comparable[][]> item : resultList) {
                    for (Comparable[] body : item.get(proxy.getPrefix())) {
                        allBodies.add(ArrayUtils.addAll(new Comparable[] { new TableRowHeader(++n, item) }, body));
                    }
                }
                if(append && n == start){continue;}
                CreateReportLogic.writeCsv(new File(targetPath).toPath().resolve(proxy.getTitle() + ".csv").toFile(), ArrayUtils.addAll(new String[] { "No." }, proxy.header()), allBodies, append);
            }
            return n;
        } catch (IOException e) {
            LOG.get().warn("出撃ログの書き込みに失敗しました", e);
            return n;
        }
    }

    private static BattleResult createBattleResult(BattleExDto dto, DataFile file, int n, boolean isLoadCombatLog) {
        return new BattleResult(dto, file, n,BattleLogProxy.get().bodyMT(dto),BuiltinScriptKt.AllBody(dto),isLoadCombatLog ? CombatLogProxy.bodyAllMT(dto) : null);
    }
    private static BattleResult createBattleResultWithCombatLog(BattleExDto dto, DataFile file, int n,Comparable[] battleLog,Map<String,String[][]> builtinCombatExtData, Map<String,Comparable[][]>combatLog) {
        return new BattleResult(dto, file, n,battleLog,builtinCombatExtData,combatLog);
    }

    public boolean isLoaded() {
        return this.isLoaded_;
    }
}
