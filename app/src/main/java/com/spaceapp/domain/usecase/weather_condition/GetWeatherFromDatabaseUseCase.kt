package com.spaceapp.domain.usecase.weather_condition

import com.spaceapp.core.common.Result
import com.spaceapp.domain.model.weather_condition.WeatherCondition
import com.spaceapp.domain.repository.WeatherConditionRepository
import com.spaceapp.domain.utils.ERROR
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.NullPointerException
import javax.inject.Inject

class GetWeatherFromDatabaseUseCase @Inject constructor(private val weatherRepository: WeatherConditionRepository) {

    operator fun invoke(): Flow<Result<WeatherCondition>> =
        flow {
            try {
                emit(Result.Loading)

                emit(Result.Success(data = weatherRepository.getWeatherConditionFromLocal()))
            } catch (e: NullPointerException) {
                emit(Result.Error(message = ERROR.INTERNET))
            } catch (e: Exception) {
                emit(Result.Error(message = e.localizedMessage ?: ERROR.UNKNOWN))
            }
        }
}