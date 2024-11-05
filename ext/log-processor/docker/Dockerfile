FROM        swr.cn-north-4.myhuaweicloud.com/ddn-k8s/docker.io/bellsoft/liberica-openjdk-debian:21.0.3-cds
LABEL       app=log-processor
RUN         addgroup --gid 1000 --system app && adduser --system --no-create-home --ingroup app --uid 1000 app
USER        1000
COPY        package/dependency     /opt/app
COPY        package/app            /opt/app
CMD         ["/opt/app/bin/log-processor"]
