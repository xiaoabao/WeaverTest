/**
 * SetDocInfoServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package com.customization.hxbank.oasys.webservice.client.oa;

public class SetDocInfoServiceLocator extends org.apache.axis.client.Service implements SetDocInfoService {

    public SetDocInfoServiceLocator() {
    }


    public SetDocInfoServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public SetDocInfoServiceLocator(String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for Domino
    private String Domino_address = "http://testzhoa.hxmis.com:80/HXBWEBOAZH/TODO_EXCHANGE_CONFIG.NSF/SetDocInfo?OpenWebService";

    public String getDominoAddress() {
        return Domino_address;
    }

    // The WSDD service name defaults to the port name.
    private String DominoWSDDServiceName = "Domino";

    public String getDominoWSDDServiceName() {
        return DominoWSDDServiceName;
    }

    public void setDominoWSDDServiceName(String name) {
        DominoWSDDServiceName = name;
    }

    public SetDocInfo getDomino() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(Domino_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getDomino(endpoint);
    }

    public SetDocInfo getDomino(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            DominoSoapBindingStub _stub = new DominoSoapBindingStub(portAddress, this);
            _stub.setPortName(getDominoWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setDominoEndpointAddress(String address) {
        Domino_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (SetDocInfo.class.isAssignableFrom(serviceEndpointInterface)) {
                DominoSoapBindingStub _stub = new DominoSoapBindingStub(new java.net.URL(Domino_address), this);
                _stub.setPortName(getDominoWSDDServiceName());
                return _stub;
            }
        }
        catch (Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        String inputPortName = portName.getLocalPart();
        if ("Domino".equals(inputPortName)) {
            return getDomino();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("urn:DefaultNamespace", "SetDocInfoService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("urn:DefaultNamespace", "Domino"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(String portName, String address) throws javax.xml.rpc.ServiceException {
        
if ("Domino".equals(portName)) {
            setDominoEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
