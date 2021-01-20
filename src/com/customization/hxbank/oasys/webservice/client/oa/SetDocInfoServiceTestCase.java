/**
 * SetDocInfoServiceTestCase.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.customization.hxbank.oasys.webservice.client.oa;

public class SetDocInfoServiceTestCase extends junit.framework.TestCase {
    public SetDocInfoServiceTestCase(String name) {
        super(name);
    }

    public void testDominoWSDL() throws Exception {
        javax.xml.rpc.ServiceFactory serviceFactory = javax.xml.rpc.ServiceFactory.newInstance();
        java.net.URL url = new java.net.URL(new SetDocInfoServiceLocator().getDominoAddress() + "?WSDL");
        javax.xml.rpc.Service service = serviceFactory.createService(url, new SetDocInfoServiceLocator().getServiceName());
        assertTrue(service != null);
    }

    public void test1DominoSETTEMPDOCINFO() throws Exception {
        DominoSoapBindingStub binding;
        try {
            binding = (DominoSoapBindingStub)
                          new SetDocInfoServiceLocator().getDomino();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertNotNull("binding is null", binding);

        // Time out after a minute
        binding.setTimeout(60000);

        // Test operation
        String value = null;
        value = binding.SETTEMPDOCINFO(new String());
        // TBD - validate results
    }

    public void test2DominoPARSEXML() throws Exception {
        DominoSoapBindingStub binding;
        try {
            binding = (DominoSoapBindingStub)
                          new SetDocInfoServiceLocator().getDomino();
        }
        catch (javax.xml.rpc.ServiceException jre) {
            if(jre.getLinkedCause()!=null)
                jre.getLinkedCause().printStackTrace();
            throw new junit.framework.AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
        }
        assertNotNull("binding is null", binding);

        // Time out after a minute
        binding.setTimeout(60000);

        // Test operation
        Object value = null;
        value = binding.PARSEXML(new String(), new String());
        // TBD - validate results
    }

}
