package com.spaceapp.domain.usecase.space_news

import com.spaceapp.domain.model.space_news.SpaceNews
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.IOException
import javax.inject.Inject
import com.spaceapp.core.common.Result
import com.spaceapp.domain.repository.SpaceNewsRepository
import com.spaceapp.domain.utils.ERROR

class GetSpaceNewsFromNetworkUseCase @Inject constructor(private val spaceNewsRepository: SpaceNewsRepository) {

    operator fun invoke(): Flow<Result<List<SpaceNews>>> = flow {
        try {
            emit(Result.Loading)

            emit(Result.Success(spaceNewsRepository.getSpaceNewsFromNetwork()))
        } catch (e: IOException) {
            emit(Result.Error(message = ERROR.INTERNET))
        } catch (e: Exception) {
            emit(
                Result.Error(
                    message = e.localizedMessage ?: ERROR.UNKNOWN
                )
            )
        }
    }
}