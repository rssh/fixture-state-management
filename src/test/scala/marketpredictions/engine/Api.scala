package marketpredictions.engine

import marketpredictions.db._

trait Api extends EngineContext 
              with UserOperations
              with PredictionOperations
{


}

// vim: set ts=4 sw=4 et:
