package com.github.elxreno.funquiz_client.di

import javax.inject.Qualifier

class Annotations {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Auth
}