
declare class Java {

    static extend(...types: any[]): any;

    static from<T>(objArray: JavaArray<T>): T[];
    static from<T>(objArray: Packages.java.util.Collection<T>): T[];

    static isJavaFunction(arg2: any): boolean;

    static isJavaMethod(arg2: any): boolean;

    static isJavaObject(arg2: any): boolean;

    static isScriptFunction(obj: any): boolean;

    static isScriptObject(obj: any): boolean;

    static isType(type: any): boolean;

    static synchronizedFunc(func: any, obj: any): any;

    static to<T>(obj: T[], objType: any): JavaArray<T>;

    static type(objTypeName: any): any;

    static typeName(type: any): any;
}