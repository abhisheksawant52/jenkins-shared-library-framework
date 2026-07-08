# Rendered by the Jenkins Shared Library Framework.
# Tokens (${...}) are substituted at build time.
FROM ${baseImage}

LABEL org.opencontainers.image.title="${appName}" \
      org.opencontainers.image.version="${version}" \
      org.opencontainers.image.source="${sourceUrl}"

WORKDIR /app

COPY ${artifact} /app/${artifact}

USER 1001

EXPOSE ${port}

ENTRYPOINT ["java", "-jar", "/app/${artifact}"]
