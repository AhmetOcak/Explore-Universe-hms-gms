package com.spaceapp.domain.usecase.mars_photo

import com.spaceapp.core.common.Result
import com.spaceapp.domain.model.mars_photos.MarsPhoto
import com.spaceapp.domain.repository.MarsPhotosRepository
import com.spaceapp.domain.utils.ERROR
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMarsPhotoFromDatabaseUseCase @Inject constructor(private val marsPhotoRepository: MarsPhotosRepository) {

    operator fun invoke(): Flow<Result<List<MarsPhoto>>> = flow {
        try {
            emit(Result.Loading)

            val data = marsPhotoRepository.getMarsPhotoFromLocal()

            if (data.isNotEmpty()) {
                emit(Result.Success(data = data))
            } else {
                emit(Result.Error(message = ERROR.INTERNET))
            }
        } catch (e: Exception) {
            emit(Result.Error(message = e.message ?: "Unknown Error"))
        }
    }
}