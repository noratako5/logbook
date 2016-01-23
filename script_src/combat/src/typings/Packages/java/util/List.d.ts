
declare module Packages.java.util {

    interface List<T> extends Collection<T> {

        [index: number]: T;

        length: number;
    }
}