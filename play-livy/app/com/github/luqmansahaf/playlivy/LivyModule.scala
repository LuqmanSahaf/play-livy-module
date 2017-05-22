package com.github.luqmansahaf.playlivy

import play.api.Configuration
import play.api.Environment
import play.api.inject.Binding
import play.api.inject.Module

/**
  * @author Luqman.
  *
  * LivyModule defines bindings for directing to module functionality
  */
class LivyModule extends Module {
  def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(
      bind[LivyManagement].to[LivyManager]
    )
  }
}
