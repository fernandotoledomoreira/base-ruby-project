FROM 758526784474.dkr.ecr.us-east-1.amazonaws.com/image-linux-ruby-backend-qa:1.5.0

COPY . /base-ruby-backend-qa
WORKDIR /base-ruby-backend-qa

RUN gem install bundler
RUN bundle install
RUN chmod 777 -R /base-ruby-backend-qa