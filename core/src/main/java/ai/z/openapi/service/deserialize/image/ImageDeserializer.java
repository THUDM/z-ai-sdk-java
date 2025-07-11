package ai.z.openapi.service.deserialize.image;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonTokenId;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ai.z.openapi.service.deserialize.BaseNodeDeserializer;
import ai.z.openapi.service.deserialize.ObjectDeserializer;
import ai.z.openapi.service.image.Image;

import java.io.IOException;

/**
 * Deserializer that can build instances of {@link Image} from any
 * JSON content, using appropriate {@link Image} type.
 */
public class ImageDeserializer extends BaseNodeDeserializer<Image> {

    private final static ObjectMapper MAPPER = new ObjectMapper();

    private final static ImageDeserializer instance = new ImageDeserializer();

    public ImageDeserializer() {
        super(Image.class, null);
    }

    public static JsonDeserializer<? extends JsonNode> getDeserializer(Class<?> nodeClass) {
        if (nodeClass == ObjectNode.class) {
            return ObjectDeserializer.getInstance();
        }
        return instance;
    }

    @Override
    public Image getNullValue(DeserializationContext ctxt) {
        return null;
    }

    @Override
    public Image deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (p.currentTokenId() == JsonTokenId.ID_START_OBJECT) {
            ObjectNode jsonNodes = deserializeObject(p, ctxt, ctxt.getNodeFactory());
            return new Image(jsonNodes);
        }
        return null;
    }
}
