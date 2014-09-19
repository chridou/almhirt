package almhirt.common.worksheets

import scala.concurrent.duration._
import scalaz.syntax.validation._
import almhirt.common._

object Futures {
 val ccuad = CanCreateUuidsAndDateTimes()         //> ccuad  : almhirt.common.CanCreateUuidsAndDateTimes = almhirt.common.CanCreat
                                                  //| eUuidsAndDateTimes$$anon$1@543c944f
              
 
 
                                          
 
 
 (1 to 109).map(_ ⇒ ccuad.getUniqueString).toList//> res0: List[String] = List(i4gV23baR4iUhzK8g8mvPQ, MuziFr6ZRWKjNaEdgjz_yA, Xl
                                                  //| iBpLAIQOCFPmPMmZ8arw, arvWSeQ7T9uN6_GzPzNFLg, tJK-szgtT1moH1G-1BLv4A, ga_e4P
                                                  //| pHTfStkZBcyAc2NA, 91w_pcJuQbq0YUUfbW6qhQ, CdJaxFwrQdWLSq-tRWnqmQ, MkC6tNCNSv
                                                  //| ONfE_6xm-QWA, WmjX-d3QSeW4TGo824K_cA, 9Djl1ca6Qkydt2zBsu-9gw, _WbX-o56Qh-h5X
                                                  //| 79rvRhqg, ztVQPvUFS7O_kBGnxaQpRA, aPj5yydHRDuM474zFKEbXw, 3-j0hNAwQ7CIIK5t-E
                                                  //| 2v1g, gogezoGJSzaRJxg0RLKLnQ, 5Ua9ji4tTna4aaYFSMZJXw, Nfcn_v0ZQkOaQCX_GLgktQ
                                                  //| , TSKINcICQZuGySuccMJg_Q, 04uSKCg0QlGJJsu1NrCSGA, 6lrXy060TW2jBXPOH0rYUw, u3
                                                  //| ib0LAqTCq-C3s2yh240w, qBnk6fDcQ1K3TrenvHNz9Q, g36Mfjl7S5Wt89k8nNELrg, Daiiv5
                                                  //| bOQDOvKDzjDfcmsg, ivIGkjH3QH-zReuF38R4Mg, 9DIP_L2dQImFiIeHqB2SSw, FNdL54cFQB
                                                  //| qvDMbkAZVAsA, GliSTvuWTmmvYoD5UH9jBw, yg1dRyS_TVSHsKss8KOuEQ, kJRdRsohTDyAQf
                                                  //| HKUIZDFw, YpGv6ARJT6CAD3z8oRXH7g, vKOYi11ERZ-NBvHAo9UDXw, dpt0GKBBRKa88tML3K
                                                  //| p9sA, v6-VrUAYTbutJXe6b5YOyQ, fSgeP1maQP2-GoWLG6bG4g, 2JuOWFiHSDKlL8bcv1SihA
                                                  //| , K6dPCgQGSgKGVyoUPkljfw
                                                  //| Output exceeds cutoff limit.
}