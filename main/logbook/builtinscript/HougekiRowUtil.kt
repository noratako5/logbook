package logbook.builtinscript

/**
 * Created by noratako5 on 2018/12/02.
 */
fun NelsonTouchEffect(at:Int,type:Int,count:Int):Int{
    return if (type==100 && count == 1) at + 2
        else if (type==100 && count == 2) at + 4
        else if ((type==101 || type == 102) && count == 2) at + 1
        else if (type==103 || type == 104) at + count
        else at
}