package almhirt

package object concurrent {
object inst extends almhirt.concurrent.AlmFutureInstances
object all extends almhirt.concurrent.AlmFutureInstances with almhirt.concurrent.ToAlmFutureOps
}