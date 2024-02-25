package lakefs

import io.lakefs.clients.sdk.ApiClient
import io.lakefs.clients.sdk.ApiException

/** Class representing an object in lakeFS. */
class StoredObject(val repoId: String,
                   val refId: String,
                   override val path: String,
                   client: ApiClient) : BaseLakeFSObject(client), WithPath {

    val stats: ObjectInfo by lazy { ObjectInfo(client.objectsApi.statObject(repoId, refId, path).execute()) }

    //    def __str__(self) -> str:
    //    return self.path
    //
    //    def __repr__(self):
    //    return f'StoredObject(repository="{self.repo}", reference="{self.ref}", path="{self.path}")'

    //    def reader(self, mode: ReadModes = 'rb', pre_sign: Optional[bool] = None) -> ObjectReader:
    //    """
    //        Context manager which provide a file-descriptor like object that allow reading the given object.
    //
    //        Usage Example:
    //
    //        .. code-block:: python
    //
    //            import lakefs
    //
    //            obj = lakefs.repository("<repository_name>").branch("<branch_name>").object("file.txt")
    //            file_size = obj.stat().size_bytes
    //
    //            with obj.reader(mode='r', pre_sign=True) as fd:
    //                # print every other 10 chars
    //                while fd.tell() < file_size
    //                    print(fd.read(10))
    //                    fd.seek(10, os.SEEK_CUR)
    //
    //        :param mode: Read mode - as supported by ReadModes
    //        :param pre_sign: (Optional), enforce the pre_sign mode on the lakeFS server. If not set, will probe server for
    //            information.
    //        :return: A Reader object
    //        """
    //    return ObjectReader(self, mode=mode, pre_sign=pre_sign, client=self._client)

    /** Returns True if object exists in lakeFS, False otherwise */
    val exists: Boolean
        get() =
            try {
                client.objectsApi.headObject(repoId, refId, path).execute()
                true
            } catch (e: ApiException) {
                false
            }

//    def copy(self, destination_branch_id: str, destination_path: str) -> WriteableObject:
//    """
//        Copy the object to a destination branch
//
//        :param destination_branch_id: The destination branch to copy the object to
//        :param destination_path: The path of the copied object in the destination branch
//        :return: The newly copied Object
//        :raise ObjectNotFoundException: if repo id,reference id, destination branch id or object path does not exist
//        :raise PermissionException: if user is not authorized to perform this operation, or operation is forbidden
//        :raise ServerException: for any other errors
//        """
//
//    with api_exception_handler():
//    object_copy_creation = lakefs_sdk.ObjectCopyCreation(src_ref=self._ref_id, src_path=self._path)
//    self._client.sdk_client.objects_api.copy_object(repository=self._repo_id,
//    branch=destination_branch_id,
//    dest_path=destination_path,
//    object_copy_creation=object_copy_creation)
//
//    return WriteableObject(repository_id=self._repo_id, reference_id=destination_branch_id, path=destination_path,
//    client=self._client)
}