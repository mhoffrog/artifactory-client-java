package org.jfrog.artifactory.client;

import org.jfrog.artifactory.client.annotation.ArtifactoryProFeature;
import org.testng.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

public class SkipProTestAnalyzer implements IInvokedMethodListener {

    // Take env var name from jfrog-testing-infra/local-rt-setup/main.go:
    public static final String JFROG_IS_OSS_VARIANT_ENV_VAR_NAME = "JFROG_TESTS_IS_OSS_VARIANT";

    @Override
    public void beforeInvocation(IInvokedMethod invokedMethod, ITestResult result) {
        if (isArtifactoryProInstalled()) {
            return;
        }
        final Method method = result.getMethod().getConstructorOrMethod().getMethod();
        if (method == null) {
            return;
        }
        if (!method.isAnnotationPresent(ArtifactoryProFeature.class)) {
            final Class<?> testClass;
            final String methodName;
            if (invokedMethod.isConfigurationMethod()) {
                testClass = result.getInstance().getClass();
                methodName = method.getName();
                //System.out.println(testClass.getName() + " - configuration method: " + methodName);
            } else if (invokedMethod.isTestMethod()) {
                testClass = invokedMethod.getTestMethod().getInstance().getClass();
                methodName = invokedMethod.getTestMethod().getMethodName();
                //System.out.println(testClass.getName() + " - test method: " + methodName);
            } else {
                testClass = result.getMethod().getInstance().getClass();
                methodName = method.getName();
                //System.out.println(testClass.getName() + " - method: " + methodName);
            }
            if (!testClass.isAnnotationPresent(ArtifactoryProFeature.class)) {
                return;
            }
        }
        throw new SkipException("This test requires an ArtifactoryPro installation");
    }

    private boolean isArtifactoryProInstalled() {
        return !Boolean.parseBoolean(System.getenv(JFROG_IS_OSS_VARIANT_ENV_VAR_NAME));
    }

}
