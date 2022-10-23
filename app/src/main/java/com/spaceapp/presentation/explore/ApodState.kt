package com.spaceapp.presentation.explore

import com.spaceapp.domain.model.Apod

sealed interface ApodState {
    data class Success(val apodData: List<Apod>?): ApodState
    data class Error(val errorMessage: String?): ApodState
    object Loading: ApodState
}