import _ from 'lodash';

function component() {
    const element = document.createElement('div');
    element.innerHTML = _.join(['Hello', 'magpiebrigde'], ' ');

    return element;
}

document.body.appendChild(component()); //this is a comment