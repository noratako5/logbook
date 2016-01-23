
interface JavaArray<T> {

    new (length: number): JavaArray<T>;

    [index: number]: T;
    length: number;
}
