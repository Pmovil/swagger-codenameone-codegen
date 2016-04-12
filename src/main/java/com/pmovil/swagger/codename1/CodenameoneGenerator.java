package com.pmovil.swagger.codename1;

import io.swagger.codegen.*;
import static io.swagger.codegen.DefaultCodegen.camelize;
import io.swagger.models.properties.*;

import java.util.*;
import java.io.File;
import org.apache.commons.lang.StringUtils;

public class CodenameoneGenerator extends DefaultCodegen implements CodegenConfig {

  // source folder where to write the files
  private static final String ENABLE_BIG_DECIMAL = "enableBigDecimal";
  private static final String ENABLE_BIG_DECIMAL_DESC = "Codenameone builtin json parser does not handle BigDecimal, but you may use BigDecimal with an alternative parser";
  protected String sourceFolder = "src";
  protected String apiVersion = "1.0.0";
  private String artifactId = "Client";
  private String groupId = "com.pmovil.swagger";
  private boolean enableBigDecimal = false;

  /**
   * Configures the type of generator.
   * 
   * @return  the CodegenType for this generator
   * @see     io.swagger.codegen.CodegenType
   */
  public CodegenType getTag() {
    return CodegenType.CLIENT;
  }

  /**
   * Configures a friendly name for the generator.  This will be used by the generator
   * to select the library with the -l flag.
   * 
   * @return the friendly name for the generator
   */
  public String getName() {
    return "Codenameone";
  }

  /**
   * Returns human-friendly help for the generator.  Provide the consumer with help
   * tips, parameters here
   * 
   * @return A string value for the help message
   */
  public String getHelp() {
    return "Generates a Codenameone client library.";
  }

  public CodenameoneGenerator() {
    super();
    cliOptions.add(CliOption.newBoolean(ENABLE_BIG_DECIMAL, ENABLE_BIG_DECIMAL_DESC));
    cliOptions.add(new CliOption(CodegenConstants.ARTIFACT_ID, CodegenConstants.ARTIFACT_ID_DESC));
    cliOptions.add(new CliOption(CodegenConstants.GROUP_ID, CodegenConstants.GROUP_ID_DESC));

    additionalProperties.put(ENABLE_BIG_DECIMAL, enableBigDecimal);
    additionalProperties.put(CodegenConstants.ARTIFACT_ID, artifactId);
    additionalProperties.put(CodegenConstants.GROUP_ID, groupId);

    // set the output folder here
    outputFolder = "generated-code/Codenameone";

    /**
     * Models.  You can write model files using the modelTemplateFiles map.
     * if you want to create one template for file, you can do so here.
     * for multiple files for model, just put another entry in the `modelTemplateFiles` with
     * a different extension
     */
    modelTemplateFiles.put(
      "model.mustache", // the template to use
      ".java");       // the extension for each file to write

    /**
     * Api classes.  You can write classes for each Api file with the apiTemplateFiles map.
     * as with models, add multiple entries with different extensions for multiple files per
     * class
     */
    apiTemplateFiles.put(
      "api.mustache",   // the template to use
      ".java");       // the extension for each file to write

    /**
     * Template Location.  This is the location which templates will be read from.  The generator
     * will use the resource stream to attempt to read the templates.
     */
    templateDir = "Codenameone";

    /**
     * Reserved words.  Override this with reserved words specific to your language
     */
    reservedWords = new HashSet<String>(
            Arrays.asList(
                // local variable names used in API methods (endpoints)
                "postBody", "path", "queryParams", "headerParams", "formParams",
                "contentTypes", "contentType", "response", "builder", "httpEntity",
                "authNames", "basePath", 

                // cn1/java reserved words
                "abstract", "continue", "for", "new", "switch", "assert",
                "default", "if", "package", "synchronized", "boolean", "do", "goto", "private",
                "this", "break", "double", "implements", "protected", "throw", "byte", "else",
                "import", "public", "throws", "case", "enum", "instanceof", "return", "transient",
                "catch", "extends", "int", "short", "try", "char", "final", "interface", "static",
                "void", "class", "finally", "long", "strictfp", "volatile", "const", "float",
                "native", "super", "while")
    );

    /**
     * Additional Properties.  These values can be passed to the templates and
     * are available in models, apis, and supporting files
     */
    additionalProperties.put("apiVersion", apiVersion);

    /**
     * Supporting Files.  You can write single files for the generator with the
     * entire object tree available.  If the input file has a suffix of `.mustache
     * it will be processed by the template engine.  Otherwise, it will be copied
     */
    // root
    supportingFiles.add(new SupportingFile("Stubber.jar", "", "Stubber.jar"));
    supportingFiles.add(new SupportingFile("build.mustache", "", "build.xml"));
    supportingFiles.add(new SupportingFile("codenameone_library_appended.properties", "", "codenameone_library_appended.properties"));
    supportingFiles.add(new SupportingFile("codenameone_library_required.properties", "", "codenameone_library_required.properties"));
    supportingFiles.add(new SupportingFile("manifest.mf", "", "manifest.mf"));
    supportingFiles.add(new SupportingFile("manifest.properties", "", "manifest.properties"));
    // lib
    supportingFiles.add(new SupportingFile("CLDC11.jar", "lib", "CLDC11.jar"));
    supportingFiles.add(new SupportingFile("CodenameOne.jar", "lib", "CodenameOne.jar"));
    // nbproject
    supportingFiles.add(new SupportingFile("project.mustache", "nbproject", "project.properties"));
    supportingFiles.add(new SupportingFile("project_xml.mustache", "nbproject", "project.xml"));
    // json client
    supportingFiles.add(new SupportingFile("BeansInterface.java", "src/com/pmovil/jsonapi", "BeansInterface.java"));
    supportingFiles.add(new SupportingFile("ConnectionRequest.java", "src/com/pmovil/jsonapi", "ConnectionRequest.java"));
    supportingFiles.add(new SupportingFile("DummyBean.java", "src/com/pmovil/jsonapi", "DummyBean.java"));
    supportingFiles.add(new SupportingFile("ErrorBean.java", "src/com/pmovil/jsonapi", "ErrorBean.java"));
    supportingFiles.add(new SupportingFile("JsonAPIClient.java", "src/com/pmovil/jsonapi", "JsonAPIClient.java"));
    supportingFiles.add(new SupportingFile("Pair.java", "src/com/pmovil/jsonapi", "Pair.java"));
    supportingFiles.add(new SupportingFile("StringWriter.java", "src/com/pmovil/jsonapi", "StringWriter.java"));
    supportingFiles.add(new SupportingFile("UnexpectedContentException.java", "src/com/pmovil/jsonapi", "UnexpectedContentException.java"));
    supportingFiles.add(new SupportingFile("UnexpectedJsonException.java", "src/com/pmovil/jsonapi", "UnexpectedJsonException.java"));
    // json simple
    supportingFiles.add(new SupportingFile("ItemList.java", "src/org/json/simple", "ItemList.java"));
    supportingFiles.add(new SupportingFile("JSONArray.java", "src/org/json/simple", "JSONArray.java"));
    supportingFiles.add(new SupportingFile("JSONAware.java", "src/org/json/simple", "JSONAware.java"));
    supportingFiles.add(new SupportingFile("JSONObject.java", "src/org/json/simple", "JSONObject.java"));
    supportingFiles.add(new SupportingFile("JSONStreamAware.java", "src/org/json/simple", "JSONStreamAware.java"));
    supportingFiles.add(new SupportingFile("JSONValue.java", "src/org/json/simple", "JSONValue.java"));
    // crypto library
    supportingFiles.add(new SupportingFile("Digest.java", "src/org/bouncycastle/crypto", "Digest.java"));
    supportingFiles.add(new SupportingFile("ExtendedDigest.java", "src/org/bouncycastle/crypto", "ExtendedDigest.java"));
    supportingFiles.add(new SupportingFile("GeneralDigest.java", "src/org/bouncycastle/crypto/digests", "GeneralDigest.java"));
    supportingFiles.add(new SupportingFile("MD5Digest.java", "src/org/bouncycastle/crypto/digests", "MD5Digest.java"));
    supportingFiles.add(new SupportingFile("Memoable.java", "src/org/bouncycastle/util", "Memoable.java"));
    
    /**
     * Language Specific Primitives.  These types will not trigger imports by
     * the client generator
     */
    languageSpecificPrimitives = new HashSet<String>(
            Arrays.asList(
                    "String",
                    "boolean",
                    "Boolean",
                    "Double",
                    "Integer",
                    "Long",
                    "Float",
                    "Object")
    );
    instantiationTypes.put("array", "ArrayList");
    instantiationTypes.put("map", "HashMap");
    
    importMapping.put("BigDecimal", "com.codename1.util.BigDecimal");
  }

  
  
  /**
   * Escapes a reserved word as defined in the `reservedWords` array. Handle escaping
   * those terms here.  This logic is only called if a variable matches the reseved words
   * 
   * @return the escaped term
   */
  @Override
  public String escapeReservedWord(String name) {
    return "_" + name;  // add an underscore to the name
  }

  /**
   * Location to write model files.  You can use the modelPackage() as defined when the class is
   * instantiated
   */
  public String modelFileFolder() {
    return outputFolder + "/" + sourceFolder + "/" + modelPackage().replace('.', File.separatorChar);
  }

  /**
   * Location to write api files.  You can use the apiPackage() as defined when the class is
   * instantiated
   */
  @Override
  public String apiFileFolder() {
    return outputFolder + "/" + sourceFolder + "/" + apiPackage().replace('.', File.separatorChar);
  }

  /**
   * Optional - type declaration.  This is a String which is used by the templates to instantiate your
   * types.  There is typically special handling for different property types
   *
   * @return a string value used as the `dataType` field for model templates, `returnType` for api templates
   */
    @Override
    public String getTypeDeclaration(Property p) {
        if (p instanceof ArrayProperty) {
            ArrayProperty ap = (ArrayProperty) p;
            Property inner = ap.getItems();
            return getSwaggerType(p) + "<" + getTypeDeclaration(inner) + ">";
        } else if (p instanceof MapProperty) {
            MapProperty mp = (MapProperty) p;
            Property inner = mp.getAdditionalProperties();
            return getSwaggerType(p) + "<String, " + getTypeDeclaration(inner) + ">";
        }
        return super.getTypeDeclaration(p);
    }

  /**
   * Optional - swagger type conversion.  This is used to map swagger types in a `Property` into 
   * either language specific types via `typeMapping` or into complex models if there is not a mapping.
   *
   * @return a string value of the type or complex model for this property
   * @see io.swagger.models.properties.Property
   */
    @Override
    public String getSwaggerType(Property p) {
        String swaggerType = super.getSwaggerType(p);
        String type = null;
        if (typeMapping.containsKey(swaggerType)) {
            type = typeMapping.get(swaggerType);
            if (languageSpecificPrimitives.contains(type)) {
                return toModelName(type);
            }
        } else {
            type = swaggerType;
        }
        return toModelName(type);
    }

    @Override
    public String toVarName(String name) {
        // replace - with _ e.g. created-at => created_at
        name = name.replaceAll("-", "_"); // FIXME: a parameter should not be assigned. Also declare the methods parameters as 'final'.

        // if it's all uppper case, do nothing
        if (name.matches("^[A-Z_]*$")) {
            return name;
        }

        // camelize (lower first character) the variable name
        // pet_id => petId
        name = camelize(name, true);

        // for reserved word or word starting with number, append _
        if (reservedWords.contains(name) || name.matches("^\\d.*")) {
            name = escapeReservedWord(name);
        }

        return name;
    }

    @Override
    public String toParamName(String name) {
        // should be the same as variable name
        return toVarName(name);
    }

    @Override
    public String toModelName(String name) {
        // model name cannot use reserved keyword, e.g. return
        if (reservedWords.contains(name)) {
            throw new RuntimeException(name + " (reserved word) cannot be used as a model name");
        }

        // camelize the model name
        // phone_number => PhoneNumber
        return camelize(name);
    }

    @Override
    public String toModelFilename(String name) {
        // should be the same as the model name
        return toModelName(name);
    }

    @Override
    public String toOperationId(String operationId) {
        // throw exception if method name is empty
        if (StringUtils.isEmpty(operationId)) {
            throw new RuntimeException("Empty method name (operationId) not allowed");
        }

        // method name cannot use reserved keyword, e.g. return
        if (reservedWords.contains(operationId)) {
            throw new RuntimeException(operationId + " (reserved word) cannot be used as method name");
        }

        return camelize(operationId, true);
    }

    @Override
    public void processOpts() {
        super.processOpts();
        
        if (additionalProperties.containsKey(CodegenConstants.ARTIFACT_ID)) {
            artifactId = (String) additionalProperties.get(CodegenConstants.ARTIFACT_ID);
        } else {
            additionalProperties.put(CodegenConstants.ARTIFACT_ID, artifactId);
        }
        if (additionalProperties.containsKey(CodegenConstants.GROUP_ID)) {
            groupId = (String) additionalProperties.get(CodegenConstants.GROUP_ID);
        } else {
            additionalProperties.put(CodegenConstants.GROUP_ID, groupId);
        }
        if (additionalProperties.containsKey(ENABLE_BIG_DECIMAL)) {
            enableBigDecimal = Boolean.valueOf(additionalProperties.get(ENABLE_BIG_DECIMAL).toString());
        } else {
            additionalProperties.put(ENABLE_BIG_DECIMAL, enableBigDecimal);
        }
        modelPackage = groupId + ".model";
        apiPackage = groupId + ".api";

        //supportingFiles.add(new SupportingFile("client.mustache", "src/" + groupId.replace('.', File.separatorChar), artifactId + ".java"));
    }

    @Override
    public void postProcessModelProperty(CodegenModel model, CodegenProperty property) {
        if (!enableBigDecimal) {
            if (property.baseType.equals("BigDecimal")) {
                // Codenameone builtin json parser does not handle BigDecimal, using Double
                property.baseType = "Double";
                property.complexType = "Double";
                property.datatype = "Double";
                property.datatypeWithEnum = "Double";
            }
        }
    }

    @Override
    public void postProcessParameter(CodegenParameter parameter) {
        if (!enableBigDecimal) {
            if (parameter.dataType.equals("BigDecimal")) {
                // Codenameone builtin json parser does not handle BigDecimal, using Double
                parameter.dataType = "Double";
            }
        }
    }
}