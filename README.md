Odoo Java Api
================

This repo has been forked because of the project https://github.com/DeBortoliWines/openerp-java-api/issues/31 

A Java API to connect to Odoo and manage data using the XMLRPC interface.

The API reduces the amount of boiler plate code needed to communicate with Odoo 
by doing error checking and type conversions.


The Api IS known to work perfectly :
* openerp-java-api-1.3.0 works perfectly up to OpenERP v7 : https://github.com/DeBortoliWines/openerp-java-api/
* openerp-java-api-2.0.x si supposed to work with the new API introduced in odoo v8 : https://github.com/DeBortoliWines/openerp-java-api/
** Take care, the package name have replace openerp by odoo so that compatibility is broken
* openerp-java-api-3.0.x , package renamed, tested on Odoo v10 and Odoov12
** purposed is to be pushed on maven for pentaho integration

For more information, including a Wiki please see the project on SourceForge: 
[https://sourceforge.net/projects/openerpjavaapi/](https://sourceforge.net/projects/openerpjavaapi)

The project is realeased under the Apache V2 license starting from version 1.5.  
Earlier versions are dual licenced and developers can choose between LGPL3 (original license) and Apache V2.


# Dependencies

This project depends on the XMLRPC library by Apache: http://ws.apache.org/xmlrpc

Download the latest version from apache and extract the tar.

The required jar files are:
xmlrpc-client-3.1.3.jar
xmlrpc-common-3.1.3.jar
ws-commons-util-1.0.2.jar

Add those jar files to your classpath and you should be ok.

# Examples

## Context manipulation

Values must be passed as string

```
    Map inputMap = new java.util.HashMap();
    inputMap.put("active_id", move_id.toString());
    openERPSession.getContext().putAll(inputMap);
```
    
## Get Many2one results

```
/**
* Get id of a Many2One property of an object row. Static method.
*
* @param row object row
* @param property should be something like "xxx_id"
* @return propertyId the id of the property
*/
public static Integer getMany2OneId(final Row row, final String property) {
if (row != null && property != null) {
Object[] propertyIdName = (Object[])row.get(property);
// propertyIdName[0] = id of Many2One linked object
// propertyIdName[1] = name of Many2One linked object
return (propertyIdName == null ? null : (Integer)propertyIdName[0]);
}
return null;
}
```

## Execute command

```
openERPSession.executeCommand("stock.inventory", "action_done", 
        new Object[]{Integer.valueOf(inventory_id.toString())});
```


    
# Other ressources [legacy]

* https://sourceforge.net/p/openerpjavaapi/wiki/Dependencies/
* https://sourceforge.net/p/openerpjavaapi/wiki/Examples/
