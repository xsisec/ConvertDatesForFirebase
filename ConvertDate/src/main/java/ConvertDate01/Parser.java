package ConvertDate01;

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.RawValue;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;

public class Parser {
    private static final DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss").withZone(TimeZone.getTimeZone("Europe/Stockholm").toZoneId());
    private static final String PUBLISHED = "published";
    private static final String SECONDS = "seconds";
    private static final String NANOSECONDS = "nanoseconds";

    public void parseDates() throws IOException {
        File file = new File(
            getClass().getClassLoader().getResource("myjson2.json").getFile()
        );
        FileReader fileReader = new FileReader(file.getAbsoluteFile());

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonObject = objectMapper.readValue(fileReader, JsonNode.class);
        List<JsonNode> publishedParents = jsonObject.findParents(PUBLISHED);
        for (JsonNode publishedParent : publishedParents) {
            JsonNode published = publishedParent.findValue(PUBLISHED);
            long seconds = 0;
            long nanos = 0;
            JsonNode secondsNode = published.findValue(SECONDS);
            JsonNode nanosecondsNode = published.findValue(NANOSECONDS);
            if (secondsNode != null && secondsNode.canConvertToLong()) {
                seconds = secondsNode.asLong();
            }
            if (nanosecondsNode != null && nanosecondsNode.canConvertToLong()) {
                nanos = nanosecondsNode.asLong();
            }
            Instant instant = Instant.ofEpochSecond(seconds, nanos);
            ObjectNode modifiedParent = (ObjectNode) publishedParent;
            JsonNode value = JsonNodeFactory.instance.rawValueNode(new RawValue("\"" + formatter.format(instant) + "\""));
            modifiedParent.replace(PUBLISHED, value);
        }


        ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(new File("output.json"), jsonObject);
    }
}
