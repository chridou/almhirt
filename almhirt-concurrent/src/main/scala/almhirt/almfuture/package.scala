package almhirt

package object almfuture {
  object inst extends almhirt.almfuture.AlmFutureInstances
  object all extends almhirt.almfuture.AlmFutureInstances with almhirt.almfuture.ToAlmFutureOps
}