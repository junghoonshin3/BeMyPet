package kr.sjh.core.model

object Exceptions {
    class FirebaseUserIdIsNullException : Exception()
    class FirebaseUserIsNullException : Exception()
    class FireStoreUserNotExistsException : Exception()
    class FirebaseEmailVerificationIsFalseException : Exception()
    class FirebaseDisplayNameNullException : Exception()
    class FirebaseEmailNullException : Exception()
    class FirebasePhotoUrlNullException : Exception()
    class RealmUserNotLoggedInException : Exception()
    class FailedToCreateDirectoryException : IllegalStateException("Failed to create directory")

    class FailedToGetDirectoryException : IllegalStateException("Failed to get directory")
    class FailedToReadBitmapFromExternalStorageException :
        IllegalStateException("Failed to read bitmap from external storage")
}