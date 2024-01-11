# xslt
Java utilities for eXstensible Stylesheet Language Transformations (XSLT).

__xslt.Pipeline__ connects a sequence of transforms into a chain, with the output from one step being the input to the next. Each step is one of: _javax.xml.transform.Source_, _javax.xml.transform.Templates_,  _org.xml.sax.XMLFilter_. Output from the final step can be passed to one of: _java.io.OutputStream_, _org.xml.sax.ContentHandler_, _javax.xml.transform.Result_. The _transform()_ methods takes a _javax.xml.transform.Source_ object as input and runs all the steps in the pipeline. The design allows one pipeline to be connected to another.

__xslt.TeeFilter__ is an XMLFilter that will copy its input to a _org.xml.sax.ContentHandler_ object as well as passing it through to the next step. It can be used to branch from one pipeline into another.

__xslt.PipelineBuilder__ constructs a _Pipeline_ instance from an XML parameter file. 