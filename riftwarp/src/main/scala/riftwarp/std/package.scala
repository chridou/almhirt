package riftwarp

package object std {
  object funs extends WarpPackageFuns with PackageBuilderFuns
  object warpbuilder extends WarpPackageFuns with PackageBuilderFuns with PackageBuilderOps
}