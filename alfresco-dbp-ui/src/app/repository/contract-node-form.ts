
export class ContractNodeForm {

  static getDefinition(): any {
    return {
      'id': 3003,
      'name': 'ACME Contract Node Form',
      'description': 'This is a form that will display extract contract metadata for files that has been typed as acme:contract',
      'version': 1,
      'lastUpdatedBy': 1,
      'lastUpdatedByFullName': ' Administrator',
      'lastUpdated': '2018-10-31T10:15:46.496+0000',
      'stencilSetId': 0,
      'referenceId': null,
      'formDefinition': {
        'tabs': [],
        'fields': [{
          'fieldType': 'ContainerRepresentation',
          'id': 'acmecontract',
          'name': 'ACME Contract',
          'type': 'group',
          'value': null,
          'required': false,
          'readOnly': false,
          'overrideId': false,
          'colspan': 1,
          'placeholder': null,
          'minLength': 0,
          'maxLength': 0,
          'minValue': null,
          'maxValue': null,
          'regexPattern': null,
          'optionType': null,
          'hasEmptyValue': null,
          'options': null,
          'restUrl': null,
          'restResponsePath': null,
          'restIdProperty': null,
          'restLabelProperty': null,
          'tab': null,
          'className': null,
          'dateDisplayFormat': null,
          'layout': {'row': -1, 'column': -1, 'colspan': 2},
          'sizeX': 2,
          'sizeY': 1,
          'row': -1,
          'col': -1,
          'visibilityCondition': null,
          'numberOfColumns': 2,
          'fields': {
            '1': [{
              'fieldType': 'FormFieldRepresentation',
              'id': 'contractid',
              'name': 'Contract ID',
              'type': 'text',
              'value': null,
              'required': false,
              'readOnly': false,
              'overrideId': false,
              'colspan': 1,
              'placeholder': null,
              'minLength': 0,
              'maxLength': 0,
              'minValue': null,
              'maxValue': null,
              'regexPattern': null,
              'optionType': null,
              'hasEmptyValue': null,
              'options': null,
              'restUrl': null,
              'restResponsePath': null,
              'restIdProperty': null,
              'restLabelProperty': null,
              'tab': null,
              'className': null,
              'params': {'existingColspan': 1, 'maxColspan': 2},
              'dateDisplayFormat': null,
              'layout': {'row': -1, 'column': -1, 'colspan': 1},
              'sizeX': 1,
              'sizeY': 1,
              'row': -1,
              'col': -1,
              'visibilityCondition': null
            }, {
              'fieldType': 'FormFieldRepresentation',
              'id': 'securityclassification',
              'name': 'Security Classification',
              'type': 'text',
              'value': null,
              'required': false,
              'readOnly': false,
              'overrideId': false,
              'colspan': 1,
              'placeholder': null,
              'minLength': 0,
              'maxLength': 0,
              'minValue': null,
              'maxValue': null,
              'regexPattern': null,
              'optionType': null,
              'hasEmptyValue': null,
              'options': null,
              'restUrl': null,
              'restResponsePath': null,
              'restIdProperty': null,
              'restLabelProperty': null,
              'tab': null,
              'className': null,
              'params': {'existingColspan': 1, 'maxColspan': 2},
              'dateDisplayFormat': null,
              'layout': {'row': -1, 'column': -1, 'colspan': 1},
              'sizeX': 1,
              'sizeY': 1,
              'row': -1,
              'col': -1,
              'visibilityCondition': null
            }, {
              'fieldType': 'FormFieldRepresentation',
              'id': 'author',
              'name': 'Author',
              'type': 'text',
              'value': null,
              'required': false,
              'readOnly': false,
              'overrideId': false,
              'colspan': 1,
              'placeholder': null,
              'minLength': 0,
              'maxLength': 0,
              'minValue': null,
              'maxValue': null,
              'regexPattern': null,
              'optionType': null,
              'hasEmptyValue': null,
              'options': null,
              'restUrl': null,
              'restResponsePath': null,
              'restIdProperty': null,
              'restLabelProperty': null,
              'tab': null,
              'className': null,
              'params': {'existingColspan': 1, 'maxColspan': 2},
              'dateDisplayFormat': null,
              'layout': {'row': -1, 'column': -1, 'colspan': 1},
              'sizeX': 1,
              'sizeY': 1,
              'row': -1,
              'col': -1,
              'visibilityCondition': null
            }],
            '2': [{
              'fieldType': 'FormFieldRepresentation',
              'id': 'contractname',
              'name': 'Contract Name',
              'type': 'text',
              'value': null,
              'required': false,
              'readOnly': false,
              'overrideId': false,
              'colspan': 1,
              'placeholder': null,
              'minLength': 0,
              'maxLength': 0,
              'minValue': null,
              'maxValue': null,
              'regexPattern': null,
              'optionType': null,
              'hasEmptyValue': null,
              'options': null,
              'restUrl': null,
              'restResponsePath': null,
              'restIdProperty': null,
              'restLabelProperty': null,
              'tab': null,
              'className': null,
              'params': {'existingColspan': 1, 'maxColspan': 1},
              'dateDisplayFormat': null,
              'layout': {'row': -1, 'column': -1, 'colspan': 1},
              'sizeX': 1,
              'sizeY': 1,
              'row': -1,
              'col': -1,
              'visibilityCondition': null
            }, {
              'fieldType': 'FormFieldRepresentation',
              'id': 'id',
              'name': 'Alfresco Node ID',
              'type': 'text',
              'value': null,
              'required': false,
              'readOnly': true,
              'overrideId': true,
              'colspan': 1,
              'placeholder': null,
              'minLength': 0,
              'maxLength': 0,
              'minValue': null,
              'maxValue': null,
              'regexPattern': null,
              'optionType': null,
              'hasEmptyValue': null,
              'options': null,
              'restUrl': null,
              'restResponsePath': null,
              'restIdProperty': null,
              'restLabelProperty': null,
              'tab': null,
              'className': null,
              'params': {'existingColspan': 1, 'maxColspan': 1},
              'dateDisplayFormat': null,
              'layout': {'row': -1, 'column': -1, 'colspan': 1},
              'sizeX': 1,
              'sizeY': 1,
              'row': -1,
              'col': -1,
              'visibilityCondition': null
            }]
          }
        }, {
          'fieldType': 'ContainerRepresentation',
          'id': 'alfrescoauditdata',
          'name': 'Alfresco Audit Data',
          'type': 'group',
          'value': null,
          'required': false,
          'readOnly': false,
          'overrideId': false,
          'colspan': 1,
          'placeholder': null,
          'minLength': 0,
          'maxLength': 0,
          'minValue': null,
          'maxValue': null,
          'regexPattern': null,
          'optionType': null,
          'hasEmptyValue': null,
          'options': null,
          'restUrl': null,
          'restResponsePath': null,
          'restIdProperty': null,
          'restLabelProperty': null,
          'tab': null,
          'className': null,
          'dateDisplayFormat': null,
          'layout': {'row': -1, 'column': -1, 'colspan': 2},
          'sizeX': 2,
          'sizeY': 1,
          'row': -1,
          'col': -1,
          'visibilityCondition': null,
          'numberOfColumns': 2,
          'fields': {
            '1': [{
              'fieldType': 'FormFieldRepresentation',
              'id': 'creator',
              'name': 'Creator',
              'type': 'text',
              'value': null,
              'required': false,
              'readOnly': true,
              'overrideId': false,
              'colspan': 1,
              'placeholder': null,
              'minLength': 0,
              'maxLength': 0,
              'minValue': null,
              'maxValue': null,
              'regexPattern': null,
              'optionType': null,
              'hasEmptyValue': null,
              'options': null,
              'restUrl': null,
              'restResponsePath': null,
              'restIdProperty': null,
              'restLabelProperty': null,
              'tab': null,
              'className': null,
              'params': {'existingColspan': 1, 'maxColspan': 2},
              'dateDisplayFormat': null,
              'layout': {'row': -1, 'column': -1, 'colspan': 1},
              'sizeX': 1,
              'sizeY': 1,
              'row': -1,
              'col': -1,
              'visibilityCondition': null
            }, {
              'fieldType': 'FormFieldRepresentation',
              'id': 'modifier',
              'name': 'Modifier',
              'type': 'text',
              'value': null,
              'required': false,
              'readOnly': true,
              'overrideId': false,
              'colspan': 1,
              'placeholder': null,
              'minLength': 0,
              'maxLength': 0,
              'minValue': null,
              'maxValue': null,
              'regexPattern': null,
              'optionType': null,
              'hasEmptyValue': null,
              'options': null,
              'restUrl': null,
              'restResponsePath': null,
              'restIdProperty': null,
              'restLabelProperty': null,
              'tab': null,
              'className': null,
              'params': {'existingColspan': 1, 'maxColspan': 2},
              'dateDisplayFormat': null,
              'layout': {'row': -1, 'column': -1, 'colspan': 1},
              'sizeX': 1,
              'sizeY': 1,
              'row': -1,
              'col': -1,
              'visibilityCondition': null
            }],
            '2': [{
              'fieldType': 'FormFieldRepresentation',
              'id': 'created',
              'name': 'Created',
              'type': 'text',
              'value': null,
              'required': false,
              'readOnly': true,
              'overrideId': false,
              'colspan': 1,
              'placeholder': null,
              'minLength': 0,
              'maxLength': 0,
              'minValue': null,
              'maxValue': null,
              'regexPattern': null,
              'optionType': null,
              'hasEmptyValue': null,
              'options': null,
              'restUrl': null,
              'restResponsePath': null,
              'restIdProperty': null,
              'restLabelProperty': null,
              'tab': null,
              'className': null,
              'params': {'existingColspan': 1, 'maxColspan': 1},
              'dateDisplayFormat': null,
              'layout': {'row': -1, 'column': -1, 'colspan': 1},
              'sizeX': 1,
              'sizeY': 1,
              'row': -1,
              'col': -1,
              'visibilityCondition': null
            }, {
              'fieldType': 'FormFieldRepresentation',
              'id': 'modified',
              'name': 'Modified',
              'type': 'text',
              'value': null,
              'required': false,
              'readOnly': true,
              'overrideId': false,
              'colspan': 1,
              'placeholder': null,
              'minLength': 0,
              'maxLength': 0,
              'minValue': null,
              'maxValue': null,
              'regexPattern': null,
              'optionType': null,
              'hasEmptyValue': null,
              'options': null,
              'restUrl': null,
              'restResponsePath': null,
              'restIdProperty': null,
              'restLabelProperty': null,
              'tab': null,
              'className': null,
              'params': {'existingColspan': 1, 'maxColspan': 1},
              'dateDisplayFormat': null,
              'layout': {'row': -1, 'column': -1, 'colspan': 1},
              'sizeX': 1,
              'sizeY': 1,
              'row': -1,
              'col': -1,
              'visibilityCondition': null
            }]
          }
        }],
        'outcomes': [],
        'javascriptEvents': [],
        'className': '',
        'style': '',
        'customFieldTemplates': {},
        'metadata': {},
        'variables': [],
        'customFieldsValueInfo': {},
        'gridsterForm': false
      }
    };
  }
}
