package lakefs

import io.lakefs.clients.sdk.ApiException

class BadRequestException: ApiException()
class NotAuthorizedException: ApiException()
class ForbiddenException: ApiException()
class NotFoundException: ApiException()
class UnsupportedOperationException: ApiException()
class ConflictException: ApiException()
class InvalidRangeException: ApiException()