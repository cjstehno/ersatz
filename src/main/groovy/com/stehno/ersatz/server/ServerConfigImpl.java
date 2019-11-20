package com.stehno.ersatz.server;

import com.stehno.ersatz.*;
import com.stehno.ersatz.impl.ExpectationsImpl;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import io.undertow.Undertow;
import org.xnio.Options;
import space.jasan.support.groovy.closure.ConsumerWithDelegate;

import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static groovy.lang.Closure.DELEGATE_FIRST;
import static io.undertow.UndertowOptions.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ServerConfigImpl implements ServerConfig {

    private static final int EPHEMERAL_PORT = 0;

    private final RequestDecoders globalDecoders = new RequestDecoders();
    private final ResponseEncoders globalEncoders = new ResponseEncoders();
    private final ExpectationsImpl expectations = new ExpectationsImpl(globalDecoders, globalEncoders);
    private boolean autoStartEnabled = true;
    private boolean mismatchToConsole;
    private boolean httpsEnabled;
    private URL keystoreLocation;
    private String keystorePass = "ersatz";
    private AuthenticationConfig authenticationConfig;
    private int desiredHttpPort = EPHEMERAL_PORT;
    private int desiredHttpsPort = EPHEMERAL_PORT;
    private Consumer<Undertow.Builder> timeoutConfig = builder -> {
    };

    @Override public ServerConfig https(boolean enabled) {
        this.httpsEnabled = true;
        return this;
    }

    @Override public ServerConfig https() {
        return https(true);
    }

    @Override public ServerConfig autoStart(boolean enabled) {
        this.autoStartEnabled = enabled;
        return this;
    }

    @Override public ServerConfig autoStart() {
        return autoStart(true);
    }

    @Override public ServerConfig timeout(int value, TimeUnit units) {
        timeoutConfig = builder -> {
            Integer ms = (int) MILLISECONDS.convert(value, units);
            builder.setServerOption(IDLE_TIMEOUT, ms);
            builder.setServerOption(NO_REQUEST_TIMEOUT, ms);
            builder.setServerOption(REQUEST_PARSE_TIMEOUT, ms);
            builder.setSocketOption(Options.READ_TIMEOUT, ms);
            builder.setSocketOption(Options.WRITE_TIMEOUT, ms);
        };
        return this;
    }

    @Override public ServerConfig timeout(int value) {
        return timeout(value, SECONDS);
    }

    @Override public ServerConfig reportToConsole() {
        return reportToConsole(true);
    }

    @Override public ServerConfig reportToConsole(boolean toConsole) {
        mismatchToConsole = toConsole;
        return this;
    }

    @Override public ServerConfig keystore(URL location, String password) {
        keystoreLocation = location;
        keystorePass = password;
        return this;
    }

    @Override public ServerConfig keystore(URL location) {
        return keystore(location, "ersatz");
    }

    @Override public ServerConfig expectations(Consumer<Expectations> expects) {
        expects.accept(expectations);

        if (autoStartEnabled) {
            move expectations to ErsatzServer (as with e-2 work)
            no longer allowed to create expectations at server create time
            start();
        }

        return this;
    }

    @Override
    public ServerConfig expectations(@DelegatesTo(value = Expectations.class, strategy = DELEGATE_FIRST) Closure closure) {
        return expectations(ConsumerWithDelegate.create(closure));
    }

    @Override public Expectations expects() {
        return expectations;
    }

    @Override
    public ServerConfig decoder(String contentType, BiFunction<byte[], DecodingContext, Object> decoder) {
        globalDecoders.register(contentType, decoder);
        return this;
    }

    @Override
    public ServerConfig decoder(ContentType contentType, BiFunction<byte[], DecodingContext, Object> decoder) {
        globalDecoders.register(contentType, decoder);
        return this;
    }

    @Override public ServerConfig encoder(String contentType, Class objectType, Function<Object, String> encoder) {
        globalEncoders.register(contentType, objectType, encoder);
        return this;
    }

    @Override public ServerConfig encoder(ContentType contentType, Class objectType, Function<Object, String> encoder) {
        globalEncoders.register(contentType, objectType, encoder);
        return this;
    }

    @Override
    public ServerConfig authentication(@DelegatesTo(value = AuthenticationConfig.class, strategy = DELEGATE_FIRST) Closure closure) {
        return authentication(ConsumerWithDelegate.create(closure));
    }

    @Override public ServerConfig authentication(Consumer<AuthenticationConfig> config) {
        authenticationConfig = new AuthenticationConfig();
        config.accept(authenticationConfig);
        return this;
    }

    @Override public ServerConfig httpPort(int value) {
        desiredHttpPort = value;
        return this;
    }

    @Override public ServerConfig httpsPort(int value) {
        desiredHttpsPort = value;
        return this;
    }

    public void clearExpectations() {
        expectations.clear();
    }

    public boolean verify(final long timeout, final TimeUnit unit) {
        return expectations.verify(timeout, unit);
    }

    public int getConfiguredHttpPort() {
        return desiredHttpPort;
    }

    public int getConfiguredHttpsPort() {
        return desiredHttpsPort;
    }

    public boolean isHttpsEnabled() {
        return httpsEnabled;
    }

    public ExpectationsImpl getExpectations(){
        return expectations;
    }

    public boolean isMismatchToConsole() {
        return mismatchToConsole;
    }

    // FIXME: this is server-specific
    public Consumer<Undertow.Builder> getTimeoutConfig() {
        return timeoutConfig;
    }

    public AuthenticationConfig getAuthenticationConfig() {
        return authenticationConfig;
    }

    public URL getKeystoreLocation(){
        return keystoreLocation;
    }

    public String getKeystorePass(){
        return keystorePass;
    }
}
