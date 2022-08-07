/*
 * Copyright 2010 sasc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package sasc.emv;

import sasc.iso7816.TagAndLength;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import sasc.iso7816.TLVUtil;
import sasc.util.Log;
import sasc.util.Util;

/**
 * Data Object List (DOL)
 *
 * In several instances, the terminal is asked to build a flexible list of data
 * elements to be passed to the card under the card‘s direction.
 * To minimise processing within the ICC, such a list is not TLV encoded but
 * is a single constructed field built by concatenating several data
 * elements together. Since the elements of the constructed field are not
 * TLV encoded, it is imperative that the ICC knows the format of this field
 * when the data is received. This is achieved by including a
 * Data Object List (DOL) in the ICC, specifying the format of the data to
 * be included in the constructed field.
 *
 * DOLs currently used in this specification include:
 * - the Processing Options Data Object List (PDOL) used with the GET PROCESSING OPTIONS command
 * - the Card Risk Management Data Object Lists (CDOL1 and CDOL2) used with the GENERATE APPLICATION CRYPTOGRAM (AC) command
 * - the Transaction Certificate Data Object List (TDOL) used to generate a TC Hash Value
 * - the Dynamic Data Authentication Data Object List (DDOL) used with the INTERNAL AUTHENTICATE command
 *
 * ---------------------------------------------------------------------------
 *
 * In other words, a DOL is sent from the ICC. This DOL contains only Tag ID bytes and length bytes.
 * The Terminal constructs the response, which contains only the VALUES for these tags.
 *
 * //TODO check DOL processing on page 55
 *
 * @author sasc
 */
public class DOL {

    public enum Type{
        PDOL("Processing Options Data Object List"),
        CDOL1("Card Risk Management Data Object List 1"),
        CDOL2("Card Risk Management Data Object List 2"),
        TDOL("Transaction Certificate Data Object List"),
        //An ICC that supports DDA shall contain a DDOL. The DDOL shall contain only the Unpredictable Number generated by the terminal (tag '9F37', 4 bytes binary).
        DDOL("Dynamic Data Authentication Data Object List");

        private String description;

        private Type(String description){
            this.description = description;
        }

        public String getDescription(){
            return description;
        }

        @Override
        public String toString(){
            return getDescription();
        }
    }

    private Type type;
    private List<TagAndLength> tagAndLengthList = new ArrayList<TagAndLength>();

    public DOL(Type type, EMVIssuerAID ia, byte[] data){
        //Parse tags and lengths
        this.type = type;
        this.tagAndLengthList = TLVUtil.parseTagAndLength(ia, data);
    }

    public List<TagAndLength> getTagAndLengthList(){
        return Collections.unmodifiableList(tagAndLengthList);
    }

    @Override
    public String toString(){
        StringWriter sw = new StringWriter();
        dump(new PrintWriter(sw), 0);
        return sw.toString();
    }

    public void dump(PrintWriter pw, int indent){
        pw.println(Util.getSpaces(indent)+type.getDescription());
        String indentStr = Util.getSpaces(indent+Log.INDENT_SIZE);

        for(TagAndLength tagAndLength : tagAndLengthList){
            int length = tagAndLength.getLength();
            pw.println(indentStr+tagAndLength.getTag().getName() + " ("+length+ " "+(length==1?"byte":"bytes")+")");
        }
    }

}
