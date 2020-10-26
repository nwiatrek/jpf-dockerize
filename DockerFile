FROM store/oracle/serverjre:1.8.0_241-b07

RUN mkdir "/opt/gradle" \
    && mkdir "jpf"

# copy the zip files
COPY ./gradle-5.4.1-bin.zip /opt/gradle
COPY ./jpf.zip /jpf

# utilities
RUN yum -y install unzip \
    && yum -y install vi \
    && yum -y install git

#install gradle
RUN unzip /opt/gradle/gradle-5.4.1-bin.zip \
    && unzip /jpf/jpf.zip
ENV PATH="/gradle-5.4.1/bin:${PATH}"
ENV PS1="\w \d :"
ENV CLASSPATH="/jpf/jpf-core/build/"
WORKDIR /jpf/jpf-core
RUN ./gradlew buildJars

WORKDIR /code