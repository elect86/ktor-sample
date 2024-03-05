package lakefs

import io.lakefs.clients.sdk.ApiException

class BadRequestException(throwable: Throwable = ApiException()): ApiException(throwable)
class NotAuthorizedException(throwable: Throwable = ApiException()): ApiException(throwable)
class ForbiddenException(throwable: Throwable = ApiException()): ApiException(throwable)
class NotFoundException(throwable: Throwable = ApiException()): ApiException(throwable)
class UnsupportedOperationException(throwable: Throwable = ApiException()): ApiException(throwable)
class ConflictException(throwable: Throwable = ApiException()): ApiException(throwable)
class InvalidRangeException(throwable: Throwable = ApiException()): ApiException(throwable)

inline fun <R> withApiExceptionHandler(block: () -> R): R =
    try {
        block()
    } catch (ex: ApiException) {
        throw ex.specificException
    }

val ApiException.specificException
    get() = when (message!!.substringAfter("Message: ").substringBefore('\n')) {
        "Bad Request" -> BadRequestException(this)
        "Not Authorized" -> NotAuthorizedException(this)
        "Forbidden" -> ForbiddenException(this)
        "Not Found" -> NotFoundException(this)
        "Unsupported Operation" -> UnsupportedOperationException(this)
        "Conflict" -> ConflictException(this)
        "Invalid Range" -> InvalidRangeException(this)
        else -> error("invalid message: $message")
    }