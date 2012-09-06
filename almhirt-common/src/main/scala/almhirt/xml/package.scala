package almhirt

package object xml {
  object xmlsyntax extends ToXmlOps
  object xmlfunctions extends XmlFunctions
  object xmlimports extends XmlFunctions with ToXmlOps
}