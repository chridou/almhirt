package almhirt.snapshots

import scalaz.syntax.validation._
import scalaz.Validation.FlatMap._
import almhirt.common._
import almhirt.aggregates.AggregateRootVersion

trait SnapshottingPolicyProvider extends Function1[String, AlmValidation[SnapshottingPolicy]] {
  final def apply(ident: String): AlmValidation[SnapshottingPolicy] = policyFor(ident)

  def policyFor(ident: String): AlmValidation[SnapshottingPolicy]
}

object SnapshottingPolicyProvider {
  def apply(policiesByIdent: Map[String, SnapshottingPolicy]): SnapshottingPolicyProvider = new SnapshottingPolicyProvider {
    override def policyFor(ident: String): AlmValidation[SnapshottingPolicy] =
      policiesByIdent get ident match {
        case Some(policy) ⇒ policy.success
        case None         ⇒ NotFoundProblem(s"""No policy for "$ident" found.""").failure
      }
  }

  val alwaysSnapshootAll: SnapshottingPolicyProvider = new SnapshottingPolicyProvider {
    override def policyFor(ident: String): AlmValidation[SnapshottingPolicy] =
      SnapshottingPolicy.AlwaysSnapshoot.success
  }

  val neverSnapshootAny: SnapshottingPolicyProvider = new SnapshottingPolicyProvider {
    override def policyFor(ident: String): AlmValidation[SnapshottingPolicy] =
      SnapshottingPolicy.NeverSnapshoot.success
  }

  def snapshootAllAtLeastEveryNStartAtVersion(n: Int, startAt: AggregateRootVersion): SnapshottingPolicyProvider = {
    val policy = SnapshottingPolicy.snapshootAtLeastEveryNStartAtVersion(n, startAt)
    new SnapshottingPolicyProvider {
      override def policyFor(ident: String): AlmValidation[SnapshottingPolicy] =
        policy.success
    }
  }

  import almhirt.configuration._
  import com.typesafe.config.Config
  def snapshootAllByConfig(config: Config): AlmValidation[SnapshottingPolicyProvider] =
    for {
      storeEvery ← config.magicDefault[Int]("never", 0)("every")
      startAt ← config.magicOption[Long]("start-at")
    } yield {
      val sa = AggregateRootVersion(startAt getOrElse 0L)
      val policy = SnapshottingPolicy(storeEvery, sa)
      new SnapshottingPolicyProvider {
        override def policyFor(ident: String): AlmValidation[SnapshottingPolicy] =
          policy.success
      }
    }

  implicit class SnapshottingPolicyProviderOps(val self: SnapshottingPolicyProvider) extends AnyVal {
    def tryGetPolicyFor(ident: String): Option[SnapshottingPolicy] = self.policyFor(ident).toOption

    def andThen(backup: SnapshottingPolicyProvider): SnapshottingPolicyProvider = new SnapshottingPolicyProvider {
      override def policyFor(ident: String): AlmValidation[SnapshottingPolicy] =
        self.policyFor(ident) fold (
          fail ⇒ backup.policyFor(ident),
          policy ⇒ policy.success)
    }
  }
}