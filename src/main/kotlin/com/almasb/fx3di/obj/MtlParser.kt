package com.almasb.fx3di.obj

/**
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class MtlParser {

    companion object {

    }
}



//    private void read(InputStream inputStream) throws IOException {
//        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
//        String line;
//        String name = "default";
//        while ((line = br.readLine()) != null) {
//            try {
//                 if (line.startsWith("map_Kd ")) {
//                    material.setDiffuseColor(Color.WHITE);
//                    material.setDiffuseMap(loadImage(line.substring("map_Kd ".length())));
////                    material.setSelfIlluminationMap(loadImage(line.substring("map_Kd ".length())));
////                    material.setSpecularColor(Color.WHITE);
//                    modified = true;
//                    //            } else if (line.startsWith("illum ")) {
//                    //                int illumNo = Integer.parseInt(line.substring("illum ".length()));
//                    /*
//                        0    Color on and Ambient off
//                        1    Color on and Ambient on
//                        2    Highlight on
//                        3    Reflection on and Ray trace on
//                        4    Transparency: Glass on
//                             Reflection: Ray trace on
//                        5    Reflection: Fresnel on and Ray trace on
//                        6    Transparency: Refraction on
//                             Reflection: Fresnel off and Ray trace on
//                        7    Transparency: Refraction on
//                             Reflection: Fresnel on and Ray trace on
//                        8    Reflection on and Ray trace off
//                        9    Transparency: Glass on
//                             Reflection: Ray trace off
//                        10   Casts shadows onto invisible surfaces
//                     */
//                } else {
//                    //log("material line ignored for " + name + ": " + line);
//                }
//            } catch (Exception ex) {
//                Logger.getLogger(MtlReader.class.getName()).log(Level.SEVERE, "Failed to parse line:" + line, ex);
//            }
//        }
//        addMaterial(name);
//    }

//    private Image loadImage(String filename) {
//        filename = baseUrl + filename;
//        ObjImporter.log("Loading image from " + filename);
//        return new Image(filename);
//    }
