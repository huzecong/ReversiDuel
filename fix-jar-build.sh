folder="jar-disassemble"
jarName=$1
mkdir $folder
mv $jarName $folder/
cd $folder
jar xf $jarName
cat << EOT >> META-INF/services/org.datafx.controller.context.resource.AnnotatedControllerResourceType

org.datafx.controller.injection.InjectResourceType
org.datafx.controller.flow.context.ViewFlowContextResourceType
org.datafx.controller.flow.context.ActionHandlerResourceType
EOT
jar cmf META-INF/MANIFEST.MF ../$jarName .
cd ..
rm -rf $folder
