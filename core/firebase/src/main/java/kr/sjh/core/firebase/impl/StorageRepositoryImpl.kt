package kr.sjh.core.firebase.impl

import kr.sjh.core.firebase.service.StorageRepository

class StorageRepositoryImpl() : StorageRepository {
    override suspend fun uploadImage(
        image: ByteArray,
        imageId: String,
        bucketName: String
    ): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun deleteImage(imageId: String, bucketName: String): Result<Unit> {
        TODO("Not yet implemented")
    }
}