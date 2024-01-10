FROM node:12.2.0 as build

WORKDIR /app

ENV PATH /app/node_modules/.bin:$PATH

COPY package.json /app/package.json
COPY package-lock.json /app/package-lock.json
RUN npm install
RUN npm install -g @angular/cli@7.3.9

COPY . /app

RUN ng build --verbose=true


FROM nginx:1.15 as run

COPY --from=build /app/dist/SWTLib/ /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
