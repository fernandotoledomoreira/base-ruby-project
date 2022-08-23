FROM 758526784474.dkr.ecr.us-east-1.amazonaws.com/image-linux-ruby-backend-qa:1.2.9

COPY . /ruby-qa-automation-backend-base
WORKDIR /ruby-qa-automation-backend-base

RUN gem install bundler
RUN bundle install
RUN chmod 777 -R /ruby-qa-automation-backend-base