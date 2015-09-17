
declare module Packages.logbook.dto {

    /**
     * ‚±‚Ìí“¬ƒtƒF[ƒYŒã‚Ìƒ‰ƒ“ƒNi—\‘ª’lj
     * @author Nekopanda
     */
    class ResultRank {

        /** Š®‘SŸ—˜S */
        static PERFECT: ResultRank;

        /** Ÿ—˜S */
        static S: ResultRank;

        /** Ÿ—˜A */
        static A: ResultRank;

        /** íp“IŸ—˜B */
        static B: ResultRank;

        /** íp“I”s–kC */
        static C: ResultRank;

        /** ”s–kD */
        static D: ResultRank;

        /** ”s–kE */
        static E: ResultRank;

        rank(): string;
    }
}