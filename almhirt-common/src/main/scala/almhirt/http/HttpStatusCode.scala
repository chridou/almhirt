package almhirt.http

import almhirt.common.AlmValidation
import almhirt.common.ElementNotFoundProblem

sealed trait HttpStatusCode { def code: Int }

sealed trait HttpInformational extends HttpStatusCode
object Http_100_Continue extends HttpInformational { val code = 100 }
object Http_101_SwitchingProtocols extends HttpInformational { val code = 101 }
object Http_102_Processing extends HttpInformational { val code = 102 }

sealed trait HttpSuccess extends HttpStatusCode
object Http_200_OK extends HttpSuccess { val code = 200 }
object Http_201_Created extends HttpSuccess { val code = 201 }
object Http_202_Accepted extends HttpSuccess { val code = 202 }
object Http_203_Non_Authoritative_Information extends HttpSuccess { val code = 203 }
object Http_204_No_Content extends HttpSuccess { val code = 204 }
object Http_205_Reset_Content extends HttpSuccess { val code = 205 }
object Http_206_Partial_Content extends HttpSuccess { val code = 206 }
object Http_207_Multi_Status extends HttpSuccess { val code = 207 }
object Http_208_Already_Reported extends HttpSuccess { val code = 208 }
object Http_226_IM_Used extends HttpSuccess { val code = 226 }
object Http_230_Authentication_Successful extends HttpSuccess { val code = 230 }

sealed trait HttpRedirection extends HttpStatusCode
object Http_300_Multiple_Choices extends HttpRedirection { val code = 300 }
object Http_301_Moved_Permanently extends HttpRedirection { val code = 301 }
object Http_302_Found extends HttpRedirection { val code = 302 }
object Http_303_See_Other extends HttpRedirection { val code = 303 }
object Http_304_Not_Modified extends HttpRedirection { val code = 304 }
object Http_305_Use_Proxy extends HttpRedirection { val code = 305 }
object Http_306_Switch_Proxy extends HttpRedirection { val code = 306 }
object Http_307_Temporary_Redirect extends HttpRedirection { val code = 307 }
object Http_308_Permanent_Redirect extends HttpRedirection { val code = 308 }

sealed trait HttpError extends HttpStatusCode

sealed trait HttpClientError extends HttpError
object Http_400_Bad_Request extends HttpClientError { val code = 400 }
object Http_401_Unauthorized extends HttpClientError { val code = 401 }
object Http_402_Payment_Required extends HttpClientError { val code = 402 }
object Http_403_Forbidden extends HttpClientError { val code = 403 }
object Http_404_Not_Found extends HttpClientError { val code = 404 }
object Http_405_Method_Not_Allowed extends HttpClientError { val code = 405 }
object Http_406_Not_Acceptable extends HttpClientError { val code = 406 }
object Http_407_Proxy_Authentication_Required extends HttpClientError { val code = 407 }
object Http_408_Request_Timeout extends HttpClientError { val code = 408 }
object Http_409_Conflict extends HttpClientError { val code = 409 }
object Http_410_Gone extends HttpClientError { val code = 410 }
object Http_411_Length_Required extends HttpClientError { val code = 411 }
object Http_412_Precondition_Failed extends HttpClientError { val code = 412 }
object Http_413_Request_Entity_Too_Large extends HttpClientError { val code = 413 }
object Http_414_Request_URI_Too_Long extends HttpClientError { val code = 414 }
object Http_415_Unsupported_Media_Type extends HttpClientError { val code = 415 }
object Http_416_Requested_Range_Not_Satisfiable extends HttpClientError { val code = 416 }
object Http_417_Expectation_Failed extends HttpClientError { val code = 417 }
object Http_418_I_Am_A_Teapot extends HttpClientError { val code = 418 }
object Http_424_Method_Failure extends HttpClientError { val code = 424 }
object Http_425_Unordered_Collection extends HttpClientError { val code = 425 }
object Http_426_Upgrade_Required extends HttpClientError { val code = 426 }
object Http_428_Precondition_Required extends HttpClientError { val code = 428 }
object Http_429_Too_Many_Requests extends HttpClientError { val code = 429 }
object Http_431_Request_Header_Fields_Too_Large extends HttpClientError { val code = 431 }

sealed trait HttpServerError extends HttpError
object Http_500_Internal_Server_Error extends HttpServerError { val code = 500 }
object Http_501_Not_Implemented extends HttpServerError { val code = 501 }
object Http_502_Bad_Gateway extends HttpServerError { val code = 502 }
object Http_503_Service_Unavailable extends HttpServerError { val code = 503 }
object Http_504_Gateway_Timeout extends HttpServerError { val code = 504 }
object Http_505_HTTP_Version_Not_Supported extends HttpServerError { val code = 505 }
object Http_506_Variant_Also_Negotiates extends HttpServerError { val code = 506 }
object Http_507_Insufficient_Storage extends HttpServerError { val code = 507 }
object Http_508_Loop_Detected extends HttpServerError { val code = 508 }
object Http_509_Bandwidth_Limit_Exceeded extends HttpServerError { val code = 509 }
object Http_510_Not_Extended extends HttpServerError { val code = 510 }
object Http_511_Network_Authentication_Required extends HttpServerError { val code = 511 }
object Http_531_Access_Denied extends HttpServerError { val code = 531 }

object HttpStatusCode {
  import scalaz.std._
  import scalaz.syntax.validation._

  def tryGetCode(code: Int): Option[HttpStatusCode] = byCode.get(code)
  def getCode(code: Int): AlmValidation[HttpStatusCode] =
    option.cata(tryGetCode(code))(
      cd => cd.success,
      ElementNotFoundProblem("No HTTP status code with numeric representation of '%d' found.".format(code)).failure)

  private val byCode = List(
    Http_100_Continue,
    Http_101_SwitchingProtocols,
    Http_102_Processing,
    Http_200_OK,
    Http_201_Created,
    Http_202_Accepted,
    Http_203_Non_Authoritative_Information,
    Http_204_No_Content,
    Http_205_Reset_Content,
    Http_206_Partial_Content,
    Http_207_Multi_Status,
    Http_208_Already_Reported,
    Http_226_IM_Used,
    Http_230_Authentication_Successful,
    Http_300_Multiple_Choices,
    Http_301_Moved_Permanently,
    Http_302_Found,
    Http_303_See_Other,
    Http_304_Not_Modified,
    Http_305_Use_Proxy,
    Http_306_Switch_Proxy,
    Http_307_Temporary_Redirect,
    Http_308_Permanent_Redirect,
    Http_400_Bad_Request,
    Http_401_Unauthorized,
    Http_402_Payment_Required,
    Http_403_Forbidden,
    Http_404_Not_Found,
    Http_405_Method_Not_Allowed,
    Http_406_Not_Acceptable,
    Http_407_Proxy_Authentication_Required,
    Http_408_Request_Timeout,
    Http_409_Conflict,
    Http_410_Gone,
    Http_411_Length_Required,
    Http_412_Precondition_Failed,
    Http_413_Request_Entity_Too_Large,
    Http_414_Request_URI_Too_Long,
    Http_415_Unsupported_Media_Type,
    Http_416_Requested_Range_Not_Satisfiable,
    Http_417_Expectation_Failed,
    Http_418_I_Am_A_Teapot,
    Http_424_Method_Failure,
    Http_425_Unordered_Collection,
    Http_426_Upgrade_Required,
    Http_428_Precondition_Required,
    Http_429_Too_Many_Requests,
    Http_431_Request_Header_Fields_Too_Large,
    Http_500_Internal_Server_Error,
    Http_501_Not_Implemented,
    Http_502_Bad_Gateway,
    Http_503_Service_Unavailable,
    Http_504_Gateway_Timeout,
    Http_505_HTTP_Version_Not_Supported,
    Http_506_Variant_Also_Negotiates,
    Http_507_Insufficient_Storage,
    Http_508_Loop_Detected,
    Http_509_Bandwidth_Limit_Exceeded,
    Http_510_Not_Extended,
    Http_511_Network_Authentication_Required,
    Http_531_Access_Denied)
    .map(x => (x.code, x)).toMap
}
