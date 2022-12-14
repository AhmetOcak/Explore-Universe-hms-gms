package com.spaceapp.domain.usecase.mars_photo

import com.spaceapp.domain.model.mars_photos.MarsPhoto
import com.spaceapp.domain.repository.MarsPhotosRepository
import javax.inject.Inject

class AddMarsPhotoToDatabaseUseCase @Inject constructor(private val marsPhotoRepository: MarsPhotosRepository) {

    suspend operator fun invoke(marsPhoto: MarsPhoto) {
        marsPhoto.photos.forEach {
            marsPhotoRepository.addMarsPhotoToLocal(marsPhoto = MarsPhoto(photos = listOf(it)))
        }
    }
}