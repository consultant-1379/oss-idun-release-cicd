"""
Module containing exceptions used in RPT-RC
"""


class KeyValuePairsNotSetException(Exception):
    """Exception for missing Key/Value pairs"""
    def __init__(self, message=None):
        self.message = message
        super().__init__(self.message)

    def __str__(self):
        if self.message:
            return f'Key Value Pairs Not Set Exception, {self.message}'
        return 'Key Value Pairs Not Set Exception has been raised'


class InDirectoryNotSetException(Exception):
    """Exception for missing in_directory"""
    def __init__(self, message=None):
        self.message = message
        super().__init__(self.message)

    def __str__(self):
        if self.message:
            return f'In Directory Not Set Exception, {self.message}'
        return 'In Directory Not Set Exception has been raised'


class InFileNotSetException(Exception):
    """Exception for missing in_file"""
    def __init__(self, message=None):
        self.message = message
        super().__init__(self.message)

    def __str__(self):
        if self.message:
            return f'In File Not Set Exception, {self.message}'
        return 'In File Not Set Exception has been raised'


class PropertyConversionException(Exception):
    """Exception for Property Conversion related errors"""
    def __init__(self, message=None):
        self.message = message
        super().__init__(self.message)

    def __str__(self):
        if self.message:
            return f'Property Conversion Exception, {self.message}'
        return 'Property Conversion Exception has been raised'
