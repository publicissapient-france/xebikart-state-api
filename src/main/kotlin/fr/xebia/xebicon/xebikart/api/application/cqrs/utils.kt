package fr.xebia.xebicon.xebikart.api.application.cqrs

sealed class Either<out L, out R> {
    data class Left<out L>(val left: L) : Either<L, Nothing>()
    data class Right<out R>(val right: R) : Either<Nothing, R>()

    companion object {
        fun <R> right(value: R): Either<Nothing, R> =
                Either.Right(value)

        fun <L> left(value: L): Either<L, Nothing> =
                Either.Left(value)

        fun <R> success(value: R): Either<Nothing, R> =
                Either.Right(value)

        fun <E : Exception> failed(cause: E): Either<E, Nothing> =
                Either.Left(cause)
    }

    fun <T> fold(foldLeft: (L) -> T, foldRight: (R) -> T): T = when (this) {
        is Left -> foldLeft(this.left)
        is Right -> foldRight(this.right)
    }

}

