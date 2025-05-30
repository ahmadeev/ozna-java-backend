# Используем образ WildFly 30
FROM quay.io/wildfly/wildfly:30.0.0.Final-jdk17

# Копируем собранный .war-файл в папку деплоя WildFly
COPY java-backend-1.0-SNAPSHOT.war /opt/jboss/wildfly/standalone/deployments/

# Настраиваем порт (по умолчанию 8080)
EXPOSE 8080

# Запускаем WildFly в standalone-режиме
CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0"]
