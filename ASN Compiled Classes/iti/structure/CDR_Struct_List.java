
package com.iti.structure;
//
// This file was generated by the BinaryNotes compiler.
// See http://bnotes.sourceforge.net 
// Any modifications to this file will be lost upon recompilation of the source ASN.1. 
//

import org.bn.*;
import org.bn.annotations.*;
import org.bn.annotations.constraints.*;
import org.bn.coders.*;
import org.bn.types.*;




    @ASN1PreparedElement
    @ASN1BoxedType ( name = "CDR_Struct_List" )
    public class CDR_Struct_List implements IASN1PreparedElement {
                
            
            @ASN1SequenceOf( name = "CDR_Struct_List" , isSetOf = false)
	    private java.util.Collection<CDR_Struct> value = null; 
    
            public CDR_Struct_List () {
            }
        
            public CDR_Struct_List ( java.util.Collection<CDR_Struct> value ) {
                setValue(value);
            }
                        
            public void setValue(java.util.Collection<CDR_Struct> value) {
                this.value = value;
            }
            
            public java.util.Collection<CDR_Struct> getValue() {
                return this.value;
            }            
            
            public void initValue() {
                setValue(new java.util.LinkedList<CDR_Struct>()); 
            }
            
            public void add(CDR_Struct item) {
                value.add(item);
            }

	    public void initWithDefaults() {
	    }

        private static IASN1PreparedElementData preparedData = CoderFactory.getInstance().newPreparedElementData(CDR_Struct_List.class);
        public IASN1PreparedElementData getPreparedData() {
            return preparedData;
        }


    }
            