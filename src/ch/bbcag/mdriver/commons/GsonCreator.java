package ch.bbcag.mdriver.commons;

import com.google.gson.*;

import java.lang.reflect.Type;

public class GsonCreator {

    private static final Gson gson = new Gson();

    public static Gson create() {
        return new GsonBuilder()
                .registerTypeAdapter(MessageType.class, new MessageTypeAdapter())
                .registerTypeAdapter(Message.class, new MessageAdapter())
                .registerTypeAdapter(StateCode.class, new StateCodeTypeAdapter())
                .setDateFormat("yyyy-MM-dd HH:mm:ssSSSZ")
                .create();
    }

    private static class MessageAdapter implements JsonDeserializer<Message>, JsonSerializer<Message> {

        @Override
        public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src);
        }

        @Override
        public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonElement type = json.getAsJsonObject().get("type");
            Class<?> typeOfDeserializedObject;

            MessageType messageType = context.deserialize(type, MessageType.class);
            try {
                typeOfDeserializedObject = MessageType.class.getField(messageType.name()).getAnnotation(MessageClass.class).type();
            } catch (NoSuchFieldException e) {
                typeOfDeserializedObject =  MessageType.class;
            }
            return context.deserialize(json, typeOfDeserializedObject);
        }
    }

    private static class MessageTypeAdapter implements JsonSerializer<MessageType>, JsonDeserializer<MessageType> {

        @Override
        public MessageType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return gson.fromJson(new JsonPrimitive(json.getAsString().toUpperCase()), MessageType.class);
        }

        @Override
        public JsonElement serialize(MessageType src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.toString().toLowerCase());
        }
    }

    private static class StateCodeTypeAdapter implements JsonSerializer<StateCode>, JsonDeserializer<StateCode> {

        @Override
        public JsonElement serialize(StateCode src, Type type, JsonSerializationContext context) {
            return new JsonPrimitive(Integer.valueOf(gson.fromJson(gson.toJson(src), String.class)));
        }

        @Override
        public StateCode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return gson.fromJson(String.valueOf(json.getAsInt()), StateCode.class);
        }
    }
}
