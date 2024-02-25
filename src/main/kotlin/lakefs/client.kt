package lakefs

import io.lakefs.clients.sdk.ApiClient

/**
 * Base class for all lakeFS SDK objects, holds the client object and handles errors where no authentication method
 * found for client. Attempts to reload client dynamically in case of changes in the environment.
 */
open class BaseLakeFSObject(open val client: ApiClient) {

//    __mutex: Lock = Lock()

//    @property
//    def _client(self):
//    """
//        If client is None due to missing authentication params, try to init again. If authentication method is still
//        missing - will raise exception
//        :return: The initialized client object
//        :raise NoAuthenticationFound: If no authentication method found to configure the lakeFS client with
//        """
//    if self.__client is not None:
//    return self.__client
//
//    with _BaseLakeFSObject.__mutex:
//    if _BaseLakeFSObject.__client is None:
//    _BaseLakeFSObject.__client = Client()
//    return _BaseLakeFSObject.__client
}