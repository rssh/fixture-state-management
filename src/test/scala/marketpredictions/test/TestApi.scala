package marketpredictions.test

import java.sql.Timestamp;
import marketpredictions.engine._

class TestApi(override var now: Timestamp) extends Api
                                   with SimpleEngineContext


// vim: set ts=4 sw=4 et:
