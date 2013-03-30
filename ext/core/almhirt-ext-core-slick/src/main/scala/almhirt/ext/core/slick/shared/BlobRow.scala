package almhirt.ext.core.slick.shared

import java.util.{ UUID => JUUID }

final case class BlobRow(id: JUUID, data: Array[Byte])