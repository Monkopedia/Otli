package otli

// Stub
class Ptr<T> private constructor()

fun <T> T.adr(): Ptr<T> = error("Stub")

fun <T> Ptr<T>.get(): T = error("Stub")

fun <T> alloc(instance: T): Ptr<T> = error("Stub")

fun <T> Ptr<T>.free(): Unit = error("Stub")

