package com.shturmann.telemetry;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.LayoutBase;
import org.slf4j.Marker;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Created by igor on 22.08.14.
 */
public class LogStashLayout extends LayoutBase<ILoggingEvent>
{
    static private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ").withZone(ZoneId.systemDefault());
    static private final String[] reserved = {
            "@timestamp", "@version", "level", "logger", "message", "exception_class", "exception_message", "stacktrace"
    };

    private boolean isReserved(String name) {
        for(String s: reserved) {
            if( s.equals(name) ) return true;
        }
        return false;
    }

    private void escape(String str, StringBuilder buf) {
        if( str == null || str.isEmpty() ) {
            buf.append("\"\"");
            return;
        }

        final int n = str.length();
        buf.append("\"");

        for( int i = 0; i < n; ++i ) {
            final char ch = str.charAt(i);
            switch(ch) {
                case '\\': case '"': case '/': buf.append('\\'); buf.append(ch); break;
                case '\b': buf.append("\\b"); break;
                case '\t': buf.append("\\t"); break;
                case '\n': buf.append("\\n"); break;
                case '\f': buf.append("\\f"); break;
                case '\r': buf.append("\\r"); break;
                default  : buf.append( ch < ' ' ? String.format("\\u%04x", ch) : ch ); break;
            }
        }

        buf.append("\"");
    }

    private void kv(String k, String v, StringBuilder buf) {
        buf.append(k);
        escape(v, buf);
    }
    private void kvc(String k, String v, StringBuilder buf) {
        kv(k, v, buf);
        buf.append(",");
    }

    @Override
    public String doLayout(ILoggingEvent event) {
        StringBuilder buf = new StringBuilder("{");

        kvc("\"@timestamp\":"   , fmt.format(Instant.ofEpochMilli(event.getTimeStamp())), buf);
        buf.append("\"@version\":"); buf.append("1"); buf.append(",");
        kvc("\"level\":"        , event.getLevel().levelStr, buf);
        kvc("\"logger\":"       , event.getLoggerName(), buf);
        kv ("\"message\":"      , event.getFormattedMessage(), buf);

        if (event.getThrowableProxy() instanceof ThrowableProxy) {
            buf.append(",");
            ThrowableProxy proxy = (ThrowableProxy) event.getThrowableProxy();

            StringWriter strace = new StringWriter();
            proxy.getThrowable().printStackTrace(new PrintWriter(strace));

            kvc("\"exception_class\":"  , proxy.getClassName(), buf);
            kvc("\"exception_message\":", proxy.getMessage(), buf);
            kv ("\"stacktrace\":"       , strace.toString(), buf);
        }

        Marker marker = event.getMarker();
        if( marker != null ) {
            buf.append(",");
            kv("\"marker\":", marker.getName(), buf);
        }

        for( Map.Entry<String,String> entry: event.getMDCPropertyMap().entrySet() ) {
            String key = entry.getKey();
            if( !isReserved(key) ) {
                buf.append(",");
                escape(key, buf);
                buf.append(":");
                escape(entry.getValue(), buf);
            }
        }

        buf.append("}");
        buf.append(System.lineSeparator());

        return buf.toString();
    }
}
